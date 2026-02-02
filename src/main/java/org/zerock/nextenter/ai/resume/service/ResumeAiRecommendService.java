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
import java.util.Map;
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
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다. (resumeId=" + request.getResumeId() + ", userId="
                                + request.getUserId() + ")"));

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
        if (request.getSkills() == null
                || (request.getSkills() instanceof List && ((List<?>) request.getSkills()).isEmpty())) {
            request.setSkills(parseJsonToObject(resume.getSkills()));
        }

        // 4. educations
        if (request.getEducations() == null
                || (request.getEducations() instanceof List && ((List<?>) request.getEducations()).isEmpty())) {
            request.setEducations(parseJsonToObject(resume.getEducations()));
        }

        // 5. careers
        if (request.getCareers() == null
                || (request.getCareers() instanceof List && ((List<?>) request.getCareers()).isEmpty())) {
            request.setCareers(parseJsonToObject(resume.getCareers()));
        }

        // 6. projects (experiences)
        if (request.getProjects() == null
                || (request.getProjects() instanceof List && ((List<?>) request.getProjects()).isEmpty())) {
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

        // 9. 디버깅: 각 필드 상태 로그
        log.info("📊 [DB 이력서 필드 상태] resumeId={}", request.getResumeId());
        log.info("  - jobCategory: {}", resume.getJobCategory());
        log.info("  - extractedText: {}", resume.getExtractedText() != null ? resume.getExtractedText().length() + "글자" : "null");
        log.info("  - skills: {}", resume.getSkills() != null && !resume.getSkills().isEmpty() ? "있음" : "비어있음");
        log.info("  - educations: {}", resume.getEducations() != null && !resume.getEducations().isEmpty() ? "있음" : "비어있음");
        log.info("  - careers: {}", resume.getCareers() != null && !resume.getCareers().isEmpty() ? "있음" : "비어있음");
        log.info("  - experiences: {}", resume.getExperiences() != null && !resume.getExperiences().isEmpty() ? "있음" : "비어있음");
        log.info("  - filePath: {}", resume.getFilePath());
        log.info("  - resumeText (생성됨): {} 글자", request.getResumeText().toString().length());

        log.info("✅ 이력서 데이터 DB 보강 완료: resumeId={}", request.getResumeId());
    }

    /**
     * Resume 엔티티로부터 AI 분석용 텍스트를 생성합니다.
     * 1순위: extractedText (PDF/DOCX에서 추출한 원본)
     * 2순위: 구조화 필드(JSON)를 파싱하여 읽기 쉬운 텍스트로 변환
     */
    private String buildResumeText(Resume resume) {
        // 1순위: extractedText (원본 텍스트)
        if (resume.getExtractedText() != null && !resume.getExtractedText().trim().isEmpty()) {
            log.debug("✅ extractedText 사용 (길이: {} 글자)", resume.getExtractedText().length());
            return resume.getExtractedText();
        }

        // 2순위: 구조화 필드로부터 재구성
        log.warn("⚠️ extractedText가 없어 구조화 필드로부터 재구성 (resumeId: {})", resume.getResumeId());
        StringBuilder sb = new StringBuilder();

        // 기본 정보
        if (resume.getResumeName() != null) {
            sb.append("[이름]\n").append(resume.getResumeName()).append("\n\n");
        }
        if (resume.getJobCategory() != null) {
            sb.append("[희망 직무]\n").append(resume.getJobCategory()).append("\n\n");
        }

        // JSON 필드들을 파싱하여 텍스트로 변환
        appendJsonField(sb, "[보유 기술]", resume.getSkills());
        appendJsonField(sb, "[학력 사항]", resume.getEducations());
        appendJsonField(sb, "[경력 사항]", resume.getCareers());
        appendJsonField(sb, "[프로젝트 및 경험]", resume.getExperiences());
        appendJsonField(sb, "[자격증 및 어학]", resume.getCertificates());

        String result = sb.toString().trim();
        if (result.isEmpty()) {
            log.error("❌ 이력서에 분석 가능한 데이터가 전혀 없습니다 (resumeId: {})", resume.getResumeId());
            return null;
        }

        log.info("✅ 구조화 필드로부터 텍스트 재구성 완료 (길이: {} 글자)", result.length());
        return result;
    }

    /**
     * JSON 필드를 읽기 쉬운 텍스트로 변환하여 StringBuilder에 추가합니다.
     */
    private void appendJsonField(StringBuilder sb, String title, String jsonField) {
        if (jsonField == null || jsonField.trim().isEmpty() || jsonField.equals("[]")) {
            return;
        }

        try {
            Object parsed = objectMapper.readValue(jsonField, Object.class);
            if (parsed instanceof List) {
                List<?> list = (List<?>) parsed;
                if (!list.isEmpty()) {
                    sb.append(title).append("\n");
                    for (Object item : list) {
                        if (item instanceof String) {
                            sb.append("- ").append(item).append("\n");
                        } else {
                            // Map이나 복잡한 객체는 toString()으로 변환
                            sb.append("- ").append(item.toString()).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            // JSON 파싱 실패 시 원본 문자열 사용
            log.warn("⚠️ JSON 파싱 실패, 원본 사용: {} - {}", title, e.getMessage());
            sb.append(title).append("\n").append(jsonField).append("\n\n");
        }
    }

    /**
     * JSON 문자열을 Object로 파싱합니다.
     * 파싱 실패 시 원본 문자열을 단일 항목 리스트로 반환하여 데이터 손실을 방지합니다.
     */
    private Object parseJsonToObject(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            return parsed != null ? parsed : new ArrayList<>();
        } catch (JsonProcessingException e) {
            log.warn("⚠️ JSON 파싱 실패, 원본 문자열을 리스트로 반환: {}", json);
            // 파싱 실패 시 원본 문자열을 단일 항목 리스트로 반환
            return List.of(json);
        }
    }

    private void saveToDatabase(AiRecommendRequest request, AiRecommendResponse responseDto)
            throws JsonProcessingException {
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