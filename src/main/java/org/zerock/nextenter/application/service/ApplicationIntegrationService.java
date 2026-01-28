package org.zerock.nextenter.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.application.dto.ApplicationSummaryResponse;
import org.zerock.nextenter.apply.entity.Apply;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.interviewoffer.entity.InterviewOffer;
import org.zerock.nextenter.interviewoffer.repository.InterviewOfferRepository;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.JobPostingRepository;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지원 통합 서비스
 * Apply(일반 지원)와 InterviewOffer(면접 제안)를 하나로 통합하여 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApplicationIntegrationService {

    private final ApplyRepository applyRepository;
    private final InterviewOfferRepository interviewOfferRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    /**
     * 사용자의 모든 지원 내역 조회 (일반 지원 + 면접 제안)
     * 프론트엔드는 이 API만 호출하면 됨
     */
    public List<ApplicationSummaryResponse> getMyApplications(Long userId) {
        log.info("통합 지원 내역 조회 - userId: {}", userId);

        List<ApplicationSummaryResponse> result = new ArrayList<>();

        // 1. 일반 지원 목록
        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);
        for (Apply apply : applies) {
            JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
            Resume resume = apply.getResumeId() != null ?
                    resumeRepository.findById(apply.getResumeId()).orElse(null) : null;
            result.add(convertApplyToSummary(apply, job, resume));
        }

        // 2. 면접 제안 목록
        List<InterviewOffer> offers = interviewOfferRepository.findByUserIdOrderByOfferedAtDesc(userId);
        for (InterviewOffer offer : offers) {
            JobPosting job = jobPostingRepository.findById(offer.getJobId()).orElse(null);
            Apply apply = offer.getApplyId() != null ?
                    applyRepository.findById(offer.getApplyId()).orElse(null) : null;
            Resume resume = apply != null && apply.getResumeId() != null ?
                    resumeRepository.findById(apply.getResumeId()).orElse(null) : null;
            result.add(convertOfferToSummary(offer, job, apply, resume));
        }

        // 3. 날짜순 정렬 (최신순)
        result.sort((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()));

        log.info("통합 지원 내역 조회 완료 - 일반 지원: {}개, 면접 제안: {}개", applies.size(), offers.size());

        return result;
    }

    /**
     * Apply → ApplicationSummaryResponse 변환
     */
    private ApplicationSummaryResponse convertApplyToSummary(Apply apply, JobPosting job, Resume resume) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);
        User company = job != null ? userRepository.findById(job.getCompanyId()).orElse(null) : null;

        // 기술 스택 파싱
        List<String> skills = parseSkills(resume);

        // 프론트엔드 호환용 status 변환
        String legacyStatus = convertDocumentStatusToLegacyStatus(apply);

        return ApplicationSummaryResponse.builder()
                .id(apply.getApplyId())
                .type("APPLICATION")
                .applyId(apply.getApplyId())
                .offerId(null)
                .userId(apply.getUserId())
                .jobId(apply.getJobId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .companyName(company != null ? company.getName() : "알 수 없음")
                .location(job != null ? job.getLocation() : "")
                .deadline(job != null && job.getDeadline() != null ? job.getDeadline().toString() : "")
                .skills(skills)
                .experience("신입")  // TODO: Resume에서 계산
                .status(legacyStatus)  // 호환용
                .interviewStatus(null)  // 일반 지원은 면접 상태 없음
                .documentStatus(apply.getDocumentStatus() != null ? apply.getDocumentStatus().name() : "PENDING")
                .finalStatus(apply.getFinalStatus() != null ? apply.getFinalStatus().name() : null)
                .aiScore(apply.getAiScore())
                .appliedAt(apply.getAppliedAt())
                .reviewedAt(apply.getReviewedAt())
                .updatedAt(apply.getUpdatedAt())
                .build();
    }

    /**
     * InterviewOffer → ApplicationSummaryResponse 변환
     */
    private ApplicationSummaryResponse convertOfferToSummary(
            InterviewOffer offer, JobPosting job, Apply apply, Resume resume) {
        User user = userRepository.findById(offer.getUserId()).orElse(null);
        User company = userRepository.findById(offer.getCompanyId()).orElse(null);

        // 기술 스택 파싱
        List<String> skills = parseSkills(resume);

        // 프론트엔드 호환용 status 변환
        String legacyStatus = convertOfferToLegacyStatus(offer);

        return ApplicationSummaryResponse.builder()
                .id(offer.getOfferId())
                .type("INTERVIEW_OFFER")
                .applyId(offer.getApplyId())
                .offerId(offer.getOfferId())
                .userId(offer.getUserId())
                .jobId(offer.getJobId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .companyName(company != null ? company.getName() : "알 수 없음")
                .location(job != null ? job.getLocation() : "")
                .deadline(job != null && job.getDeadline() != null ? job.getDeadline().toString() : "")
                .skills(skills)
                .experience("신입")  // TODO: Resume에서 계산
                .status(legacyStatus)  // 호환용
                .interviewStatus(offer.getInterviewStatus().name())
                .documentStatus(apply != null && apply.getDocumentStatus() != null ? 
                               apply.getDocumentStatus().name() : "PASSED")
                .finalStatus(offer.getFinalResult() != null ? offer.getFinalResult().name() : null)
                .aiScore(apply != null ? apply.getAiScore() : null)
                .appliedAt(offer.getOfferedAt())
                .reviewedAt(null)
                .updatedAt(offer.getUpdatedAt())
                .build();
    }

    /**
     * 기술 스택 파싱
     */
    private List<String> parseSkills(Resume resume) {
        if (resume == null || resume.getSkills() == null || resume.getSkills().isEmpty()) {
            return List.of();
        }

        try {
            if (resume.getSkills().trim().startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(resume.getSkills(),
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } else {
                return Arrays.stream(resume.getSkills().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("skills 파싱 실패, 쉼표 구분으로 재시도: {}", e.getMessage());
            return Arrays.stream(resume.getSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Apply의 documentStatus → 프론트엔드 호환 status 변환
     */
    private String convertDocumentStatusToLegacyStatus(Apply apply) {
        if (apply.getFinalStatus() != null) {
            switch (apply.getFinalStatus()) {
                case PASSED: return "ACCEPTED";
                case REJECTED: return "REJECTED";
                case CANCELED: return "CANCELED";
            }
        }
        return "PENDING";
    }

    /**
     * InterviewOffer → 프론트엔드 호환 status 변환
     */
    private String convertOfferToLegacyStatus(InterviewOffer offer) {
        if (offer.getFinalResult() != null) {
            switch (offer.getFinalResult()) {
                case PASSED: return "ACCEPTED";
                case REJECTED: return "REJECTED";
            }
        }
        return "PENDING";
    }
}
