package org.zerock.nextenter.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    private List<RecommendationJobDto> callAiServer(Resume resume, RecommendationRequest request) {
        // TODO: 실제 AI 서버 통신 로직
        // AiRecommendationRequest aiRequest = AiRecommendationRequest.from(resume, request);
        // AiRecommendationResponse aiResponse = aiServerClient.getRecommendations(aiRequest);
        // return aiResponse.toJobDtos();

        // 임시 Mock 데이터 (5개)
        return List.of(
                RecommendationJobDto.builder()
                        .jobId(1L)
                        .jobTitle("백엔드 개발자")
                        .companyName("네이버")
                        .score(95)
                        .grade("S")
                        .matchReasons(List.of("Spring Boot 5년 경력 일치", "Java 전문가 수준"))
                        .missingSkills(List.of("Kafka"))
                        .location("경기 성남시")
                        .experienceLevel("경력 5년 이상")
                        .salary("5000~7000만원")
                        .build(),
                RecommendationJobDto.builder()
                        .jobId(2L)
                        .jobTitle("시니어 백엔드 개발자")
                        .companyName("카카오")
                        .score(92)
                        .grade("S")
                        .matchReasons(List.of("기술 스택 100% 일치", "프로젝트 경험 우수"))
                        .missingSkills(List.of())
                        .location("경기 성남시")
                        .experienceLevel("경력 5년 이상")
                        .salary("6000~8000만원")
                        .build(),
                RecommendationJobDto.builder()
                        .jobId(3L)
                        .jobTitle("Java 개발자")
                        .companyName("라인")
                        .score(88)
                        .grade("A")
                        .matchReasons(List.of("Spring 경험 5년 이상"))
                        .missingSkills(List.of("Redis", "MSA"))
                        .location("서울 강남구")
                        .experienceLevel("경력 3년 이상")
                        .salary("4500~6000만원")
                        .build(),
                RecommendationJobDto.builder()
                        .jobId(4L)
                        .jobTitle("풀스택 개발자")
                        .companyName("쿠팡")
                        .score(85)
                        .grade("A")
                        .matchReasons(List.of("백엔드 경험 우수", "대규모 트래픽 경험"))
                        .missingSkills(List.of("React", "Vue.js"))
                        .location("서울 송파구")
                        .experienceLevel("경력 4년 이상")
                        .salary("5000~7000만원")
                        .build(),
                RecommendationJobDto.builder()
                        .jobId(5L)
                        .jobTitle("서버 개발자")
                        .companyName("배달의민족")
                        .score(82)
                        .grade("B")
                        .matchReasons(List.of("Spring Boot 경험", "API 설계 능력"))
                        .missingSkills(List.of("Kubernetes", "Docker"))
                        .location("서울 송파구")
                        .experienceLevel("경력 3년 이상")
                        .salary("4000~5500만원")
                        .build()
        );
    }
}