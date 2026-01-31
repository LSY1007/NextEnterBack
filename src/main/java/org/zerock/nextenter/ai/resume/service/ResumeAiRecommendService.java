package org.zerock.nextenter.ai.resume.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.ai.resume.ResumeAiService;
import org.zerock.nextenter.ai.resume.dto.AiRecommendRequest;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;
import org.zerock.nextenter.ai.resume.entity.ResumeAiRecommend;
import org.zerock.nextenter.ai.resume.repository.ResumeAiRecommendRepository;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAiRecommendService {

    private final ResumeAiService resumeAiService; 
    private final ResumeAiRecommendRepository recommendRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AiRecommendResponse recommendAndSave(AiRecommendRequest request) {
        log.info("🚀 AI 추천 및 저장 프로세스 시작 (userId: {})", request.getUserId());

        // 0. resumeText가 비어있으면 DB에서 이력서 조회 후 request 보강 (웹 AI 버튼 500 해결)
        enrichRequestFromResume(request);

        // 1. 비서에게 시켜서 파이썬 서버 데이터 가져오기 (422 해결된 메서드 호출)
        AiRecommendResponse responseDto = resumeAiService.fetchRecommendation(request);
        
        // 2. 응답에 유저 정보 보강
        responseDto.setUserId(request.getUserId());

        // 3. DB 저장
        try {
            saveToDatabase(request, responseDto);
        } catch (Exception e) {
            log.error("⚠️ [DB Error] 저장 실패: {}", e.getMessage());
        }

        return responseDto;
    }

    /**
     * resumeId로 DB에서 이력서 조회 후 request를 보강한다.
     * 프론트엔드는 resumeId, userId만 전송하므로 resumeText 등이 비어있을 수 있다.
     */
    private void enrichRequestFromResume(AiRecommendRequest request) {
        if (request.getResumeText() != null && !request.getResumeText().toString().trim().isEmpty()) {
            log.debug("resumeText가 이미 존재하여 DB 조회 생략");
            return;
        }
        if (request.getResumeId() == null || request.getUserId() == null) {
            log.warn("resumeId 또는 userId가 없어 DB 보강 불가");
            return;
        }

        Resume resume = resumeRepository
                .findByResumeIdAndUserIdAndDeletedAtIsNull(request.getResumeId(), request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다. (resumeId=" + request.getResumeId() + ", userId=" + request.getUserId() + ")"));

        // 1. resumeText 구성: extractedText 우선, 없으면 구조화 필드로부터 생성
        String resumeText = buildResumeText(resume);
        if (resumeText != null && !resumeText.trim().isEmpty()) {
            request.setResumeText(resumeText);
        }

        // 2. jobCategory
        if (request.getJobCategory() == null && resume.getJobCategory() != null) {
            request.setJobCategory(resume.getJobCategory());
        }

        // 3. skills
        if (request.getSkills() == null || (request.getSkills() instanceof List && ((List<?>) request.getSkills()).isEmpty())) {
            request.setSkills(parseJsonToObject(resume.getSkills()));
        }

        // 4. educations
        if (request.getEducations() == null || (request.getEducations() instanceof List && ((List<?>) request.getEducations()).isEmpty())) {
            request.setEducations(parseJsonToObject(resume.getEducations()));
        }

        // 5. careers
        if (request.getCareers() == null || (request.getCareers() instanceof List && ((List<?>) request.getCareers()).isEmpty())) {
            request.setCareers(parseJsonToObject(resume.getCareers()));
        }

        // 6. projects (experiences)
        if (request.getProjects() == null || (request.getProjects() instanceof List && ((List<?>) request.getProjects()).isEmpty())) {
            request.setProjects(parseJsonToObject(resume.getExperiences()));
        }

        // 7. filePath (AI 서버가 파일 파싱 시 사용)
        if (request.getFilePath() == null && resume.getFilePath() != null && !resume.getFilePath().trim().isEmpty()) {
            request.setFilePath(resume.getFilePath());
        }

        // 8. resumeText가 여전히 비어있으면 사용자 안내 메시지로 예외
        if (request.getResumeText() == null || request.getResumeText().toString().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "이력서에 분석할 내용이 없습니다. 이력서를 작성하거나 파일을 업로드해주세요.");
        }

        log.info("✅ 이력서 데이터 DB 보강 완료: resumeId={}", request.getResumeId());
    }

    private String buildResumeText(Resume resume) {
        if (resume.getExtractedText() != null && !resume.getExtractedText().trim().isEmpty()) {
            return resume.getExtractedText();
        }
        StringBuilder sb = new StringBuilder();
        if (resume.getResumeName() != null) sb.append("이름: ").append(resume.getResumeName()).append("\n");
        if (resume.getJobCategory() != null) sb.append("희망 직무: ").append(resume.getJobCategory()).append("\n");
        if (resume.getSkills() != null && !resume.getSkills().trim().isEmpty()) {
            sb.append("보유 기술: ").append(resume.getSkills().trim()).append("\n");
        }
        if (resume.getEducations() != null && !resume.getEducations().trim().isEmpty()) {
            sb.append("학력: ").append(resume.getEducations().trim()).append("\n");
        }
        if (resume.getCareers() != null && !resume.getCareers().trim().isEmpty()) {
            sb.append("경력: ").append(resume.getCareers().trim()).append("\n");
        }
        if (resume.getExperiences() != null && !resume.getExperiences().trim().isEmpty()) {
            sb.append("경험/활동: ").append(resume.getExperiences().trim()).append("\n");
        }
        if (resume.getCertificates() != null && !resume.getCertificates().trim().isEmpty()) {
            sb.append("자격증/어학: ").append(resume.getCertificates().trim()).append("\n");
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private Object parseJsonToObject(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            return parsed != null ? parsed : new ArrayList<>();
        } catch (JsonProcessingException e) {
            log.warn("JSON 파싱 실패, 빈 리스트 사용: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveToDatabase(AiRecommendRequest request, AiRecommendResponse responseDto) throws JsonProcessingException {
        String fullJson = objectMapper.writeValueAsString(responseDto);

        ResumeAiRecommend entity = ResumeAiRecommend.builder()
                .resumeId(request.getResumeId())
                .userId(request.getUserId())
                .aiResponse(fullJson)
                .aiReport(responseDto.getAiReport())
                .build();

        ResumeAiRecommend saved = recommendRepository.save(entity);
        
        responseDto.setRecommendId(saved.getRecommendId());
        responseDto.setCreatedAt(saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<AiRecommendResponse> getHistoryByUserId(Long userId) {
        List<ResumeAiRecommend> histories = recommendRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return histories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private AiRecommendResponse convertToResponse(ResumeAiRecommend entity) {
        try {
            AiRecommendResponse dto = objectMapper.readValue(entity.getAiResponse(), AiRecommendResponse.class);
            dto.setRecommendId(entity.getRecommendId());
            dto.setResumeId(entity.getResumeId() != null ? String.valueOf(entity.getResumeId()) : null);
            dto.setUserId(entity.getUserId());
            dto.setCreatedAt(entity.getCreatedAt());
            return dto;
        } catch (Exception e) {
            log.error("데이터 복구 실패: {}", e.getMessage());
            return null;
        }
    }
}