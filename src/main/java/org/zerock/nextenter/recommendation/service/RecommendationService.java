package org.zerock.nextenter.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;
import org.zerock.nextenter.ai.resume.service.ResumeAiRecommendService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.recommendation.dto.*;
import org.zerock.nextenter.recommendation.entity.JobRecommendation;
import org.zerock.nextenter.recommendation.repository.JobRecommendationRepository;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendationService {

    private final JobRecommendationRepository recommendationRepository;
    private final ResumeRepository resumeRepository;
    // TODO: 나중에 CreditService 추가
    // private final CreditService creditService;
    // TODO: 나중에 AI 서버 클라이언트 추가
    // private final AiServerClient aiServerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int RECOMMENDATION_CREDIT = 50;

    /**
     * AI 추천 요청 및 결과 저장
     */
    @Transactional
    public RecommendationDto getRecommendation(Long userId, RecommendationRequest request) {
        // 1. 이력서 존재 확인
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (!resume.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 이력서만 사용 가능합니다");
        }

        // 2. 크레딧 확인 및 차감 (TODO: 나중에 구현)
        // creditService.useCredit(userId, RECOMMENDATION_CREDIT, "JOB_RECOMMENDATION",
        //                         request.getResumeId());

        // 3. AI 서버에 추천 요청 (임시로 Mock 데이터)
        List<RecommendationJobDto> aiRecommendedJobs = callAiServer(resume, request);

        // 4. 추천 결과 DB 저장
        try {
            String recommendedJobsJson = objectMapper.writeValueAsString(aiRecommendedJobs);
            String requestDataJson = objectMapper.writeValueAsString(request);

            JobRecommendation recommendation = JobRecommendation.builder()
                    .userId(userId)
                    .resumeId(request.getResumeId())
                    .recommendedJobs(recommendedJobsJson)
                    .creditUsed(RECOMMENDATION_CREDIT)
                    .requestData(requestDataJson)
                    .build();

            JobRecommendation saved = recommendationRepository.save(recommendation);

            // 5. DTO 변환 후 반환
            return RecommendationDto.from(saved, resume.getTitle());

        } catch (Exception e) {
            // 실패 시 크레딧 환불 (TODO: 나중에 구현)
            // creditService.refundCredit(userId, RECOMMENDATION_CREDIT, "JOB_RECOMMENDATION_FAILED");
            throw new RuntimeException("추천 결과 저장 실패", e);
        }
    }

    /**
     * 추천 히스토리 조회 (페이징)
     */
    public Page<RecommendationHistoryDto> getHistory(Long userId, Pageable pageable) {
        Page<JobRecommendation> recommendations =
                recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return recommendations.map(rec -> {
            try {
                List<RecommendationJobDto> jobs = objectMapper.readValue(
                        rec.getRecommendedJobs(),
                        objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, RecommendationJobDto.class)
                );

                RecommendationJobDto topJob = jobs.isEmpty() ? null : jobs.get(0);

                // Resume 제목 조회
                Resume resume = resumeRepository.findById(rec.getResumeId()).orElse(null);
                String resumeTitle = resume != null ? resume.getTitle() : "삭제된 이력서";

                return RecommendationHistoryDto.builder()
                        .recommendationId(rec.getRecommendationId())
                        .resumeId(rec.getResumeId())
                        .resumeTitle(resumeTitle)
                        .jobCount(jobs.size())
                        .topJobTitle(topJob != null ? topJob.getJobTitle() : null)
                        .topCompanyName(topJob != null ? topJob.getCompanyName() : null)
                        .topScore(topJob != null ? topJob.getScore() : null)
                        .creditUsed(rec.getCreditUsed())
                        .createdAt(rec.getCreatedAt())
                        .build();

            } catch (Exception e) {
                throw new RuntimeException("히스토리 변환 실패", e);
            }
        });
    }

    /**
     * 특정 추천 결과 상세 조회
     */
    public RecommendationDto getDetail(Long userId, Long recommendationId) {
        JobRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("추천 결과를 찾을 수 없습니다"));

        if (!recommendation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 추천 결과만 조회 가능합니다");
        }

        Resume resume = resumeRepository.findById(recommendation.getResumeId()).orElse(null);
        String resumeTitle = resume != null ? resume.getTitle() : "삭제된 이력서";

        return RecommendationDto.from(recommendation, resumeTitle);
    }

    /**
     * AI 서버 호출 (임시 Mock)
     * TODO: 나중에 실제 AI 서버 통신으로 교체
     */
    // [NEW] AI 서비스 의존성 주입
    private final ResumeAiRecommendService resumeAiRecommendService;

    /**
     * AI 서버 호출 (실제 구현)
     * ResumeAiRecommendService를 통해 AI 서버와 통신하고 결과를 변환합니다.
     */
    private List<RecommendationJobDto> callAiServer(Resume resume, RecommendationRequest request) {
        log.info("🚀 AI 서버로 추천 요청 전송: resumeId={}", resume.getResumeId());

        try {
            // 1. AI 요청 객체 생성 (AiRecommendRequest)
            // 기본값 설정 - 필요한 경우 더 정교하게 매핑 가능
            org.zerock.nextenter.ai.resume.dto.AiRecommendRequest aiRequest = new org.zerock.nextenter.ai.resume.dto.AiRecommendRequest();
            aiRequest.setResumeId(resume.getResumeId());
            aiRequest.setUserId(resume.getUserId());
            
            // ResumeAiRecommendService가 나머지 필드(resumeText 등)를 자동으로 DB에서 채워줍니다.
            // 따라서 여기서는 ID만 넘겨도 충분합니다.

            // 2. AI 서비스 호출 (실제 Python 서버 통신)
            AiRecommendResponse aiResponse = resumeAiRecommendService.recommendAndSave(aiRequest);

            if (aiResponse == null || aiResponse.getCompanies() == null) {
                log.warn("⚠️ AI 서버 응답이 없거나 추천 결과가 비어있습니다.");
                return List.of();
            }

            log.info("✅ AI 추천 완료: {}개 기업 반환됨 (등급: {})", 
                    aiResponse.getCompanies().size(), aiResponse.getGrade());

            // 3. 응답 변환 (AiRecommendResponse -> RecommendationJobDto)
            return aiResponse.getCompanies().stream()
                    .map(company -> RecommendationJobDto.builder()
                            // ID는 임시로 생성 (실제 job 테이블이 있다면 거기서 가져와야 함)
                            .jobId((long) company.getCompanyName().hashCode()) 
                            .jobTitle(aiResponse.getTargetRole()) // AI가 분석한 타겟 직무 사용
                            .companyName(company.getCompanyName())
                            .score(company.getMatchScore().intValue())
                            .grade(aiResponse.getGrade()) // 전체 이력서 등급 사용
                            .matchReasons(List.of(company.getTier() + " 기업 추천", company.getMatchType() + " 매칭"))
                            .missingSkills(company.getMissingSkills())
                            .location("-") // Python 응답에 위치 정보가 없다면 기본값
                            .experienceLevel(company.getTier()) // Tier를 경력 레벨 자리에 임시 표시
                            .salary("-") // 연봉 정보 없음
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ AI 서버 호출 중 오류 발생: {}", e.getMessage(), e);
            // 에러 시 빈 리스트 반환 (또는 예외 던지기)
            return List.of();
        }
    }
}