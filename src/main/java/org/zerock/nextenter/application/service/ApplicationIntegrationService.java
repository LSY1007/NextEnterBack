package org.zerock.nextenter.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.application.dto.ApplicationSummaryResponse;
import org.zerock.nextenter.apply.entity.Apply;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.company.entity.Company; // ✅ 추가
import org.zerock.nextenter.company.repository.CompanyRepository; // ✅ 추가
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
 * 이제 "일반 지원(Apply)" 내역만 처리합니다. (면접 제안 제외)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApplicationIntegrationService {

    private final ApplyRepository applyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository; // ✅ CompanyRepository 주입

    public List<ApplicationSummaryResponse> getMyApplications(Long userId) {
        List<ApplicationSummaryResponse> result = new ArrayList<>();

        // ✅ [변경] 면접 제안(InterviewOffer) 로직 삭제 -> 오직 내가 "지원한" 내역만 조회
        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);

        for (Apply apply : applies) {
            JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
            Resume resume = apply.getResumeId() != null ?
                    resumeRepository.findById(apply.getResumeId()).orElse(null) : null;
            result.add(convertApplyToSummary(apply, job, resume));
        }

        return result; // 이미 날짜순 정렬되어 있음
    }

    private ApplicationSummaryResponse convertApplyToSummary(Apply apply, JobPosting job, Resume resume) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);

        // ✅ [수정] 기업명 오류 해결: JobPosting의 companyId로 Company 정보 조회
        Company company = null;
        if (job != null) {
            company = companyRepository.findById(job.getCompanyId()).orElse(null);
        }

        List<String> skills = parseSkills(resume);
        String legacyStatus = convertDocumentStatusToLegacyStatus(apply);

        return ApplicationSummaryResponse.builder()
                .id(apply.getApplyId())
                .type("APPLICATION") // 타입은 APPLICATION 고정
                .applyId(apply.getApplyId())
                .offerId(null)
                .userId(apply.getUserId())
                .jobId(apply.getJobId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .jobTitle(job != null ? job.getTitle() : "삭제된 공고")
                .jobCategory(job != null ? job.getJobCategory() : "")
                // ✅ [수정] company.getCompanyName() 사용
                .companyName(company != null ? company.getCompanyName() : "알 수 없음")
                .location(job != null ? job.getLocation() : "")
                .deadline(job != null && job.getDeadline() != null ? job.getDeadline().toString() : "")
                .skills(skills)
                .experience("신입") // 필요시 resume에서 계산
                .status(legacyStatus)
                .interviewStatus(null)
                .documentStatus(apply.getDocumentStatus() != null ? apply.getDocumentStatus().name() : "PENDING")
                .finalStatus(apply.getFinalStatus() != null ? apply.getFinalStatus().name() : null)
                .aiScore(apply.getAiScore())
                .appliedAt(apply.getAppliedAt())
                .reviewedAt(apply.getReviewedAt())
                .updatedAt(apply.getUpdatedAt())
                .build();
    }

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
            return Arrays.stream(resume.getSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }

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
}