package org.zerock.nextenter.apply.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.apply.dto.ApplyListResponse;
import org.zerock.nextenter.apply.dto.ApplyRequest;
import org.zerock.nextenter.apply.dto.ApplyResponse;
import org.zerock.nextenter.apply.dto.ApplyStatusUpdateRequest;
import org.zerock.nextenter.apply.entity.Apply;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.JobPostingRepository;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApplyService {

    private final ApplyRepository applyRepository;
    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeRepository resumeRepository;

    /**
     * 지원하기 (개인회원용)
     */
    @Transactional
    public ApplyResponse createApply(Long userId, ApplyRequest request) {
        log.info("지원 등록 - userId: {}, jobId: {}, resumeId: {}",
                userId, request.getJobId(), request.getResumeId());

        // 중복 지원 확인
        boolean alreadyApplied = applyRepository.existsByUserIdAndJobId(userId, request.getJobId());
        if (alreadyApplied) {
            throw new IllegalStateException("이미 지원한 공고입니다");
        }

        // 공고 유효성 검증
        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

        if (job.getStatus() != JobPosting.Status.ACTIVE) {
            throw new IllegalStateException("마감된 공고입니다");
        }

        // 이력서 유효성 검증
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (!resume.getUserId().equals(userId)) {
            throw new IllegalArgumentException("자신의 이력서만 사용할 수 있습니다");
        }

        // 지원 생성
        Apply apply = Apply.builder()
                .userId(userId)
                .jobId(request.getJobId())
                .resumeId(request.getResumeId())
                .coverLetterId(request.getCoverLetterId())
                .status(Apply.Status.PENDING)
                .build();

        apply = applyRepository.save(apply);
        
        // 공고의 지원자 수 증가
        log.info("지원자 수 증가 전 - jobId: {}", request.getJobId());
        jobPostingRepository.incrementApplicantCount(request.getJobId());
        log.info("지원자 수 증가 후 - jobId: {}", request.getJobId());
        
        // 실제 지원자 수 확인
        Long actualCount = applyRepository.countByJobId(request.getJobId());
        log.info("실제 지원자 수 - jobId: {}, count: {}", request.getJobId(), actualCount);
        
        log.info("지원 완료 - applyId: {}", apply.getApplyId());

        return convertToDetailResponse(apply);
    }

    /**
     * 내 지원 내역 조회 (개인회원용) - 단순 목록
     */
    public List<ApplyListResponse> getMyApplies(Long userId) {
        log.info("내 지원 내역 조회 - userId: {}", userId);

        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);
        
        return applies.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 내 지원 내역 조회 (개인회원용) - 페이징
     */
    public Page<ApplyListResponse> getMyApplications(Long userId, int page, int size) {
        log.info("내 지원 내역 조회 - userId: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);
        
        // List를 Page로 변환
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), applies.size());
        List<Apply> pageContent = applies.subList(start, end);
        
        Page<Apply> applyPage = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, applies.size()
        );

        return applyPage.map(this::convertToListResponse);
    }

    /**
     * 기업의 모든 지원자 조회 (페이징)
     */
    public Page<ApplyListResponse> getAppliesByCompany(
            Long companyId, Long jobId, int page, int size) {

        log.info("기업 지원자 조회 - companyId: {}, jobId: {}", companyId, jobId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Apply> applies;

        if (jobId != null) {
            // 특정 공고의 지원자만 조회
            applies = applyRepository.findByJobIdPaged(jobId, pageable);
        } else {
            // 기업의 모든 공고에 대한 지원자 조회
            applies = applyRepository.findByCompanyId(companyId, pageable);
        }

        return applies.map(this::convertToListResponse);
    }

    /**
     * 지원자 상세 조회
     */
    public ApplyResponse getApplyDetail(Long applyId, Long companyId) {
        log.info("지원자 상세 조회 - applyId: {}, companyId: {}", applyId, companyId);

        Apply apply = applyRepository.findByIdAndCompanyId(applyId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원 내역을 찾을 수 없거나 접근 권한이 없습니다"));

        return convertToDetailResponse(apply);
    }

    /**
     * 지원 상태 변경 (합격/불합격 등)
     */
    @Transactional
    public ApplyResponse updateApplyStatus(
            Long applyId, Long companyId, ApplyStatusUpdateRequest request) {

        log.info("지원 상태 변경 - applyId: {}, status: {}", applyId, request.getStatus());

        Apply apply = applyRepository.findByIdAndCompanyId(applyId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원 내역을 찾을 수 없거나 접근 권한이 없습니다"));

        // 상태 변경
        apply.setStatus(Apply.Status.valueOf(request.getStatus()));
        apply.setNotes(request.getNotes());
        apply.setReviewedAt(LocalDateTime.now());

        applyRepository.save(apply);

        return convertToDetailResponse(apply);
    }

    // Private helper methods

    private ApplyListResponse convertToListResponse(Apply apply) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);
        JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
        Resume resume = apply.getResumeId() != null ?
                resumeRepository.findById(apply.getResumeId()).orElse(null) : null;

        // 기술 스택 파싱
        List<String> skills = resume != null && resume.getSkills() != null ?
                Arrays.stream(resume.getSkills().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()) : List.of();

        // 경력 계산 (임시 - 추후 Resume에서 파싱)
        String experience = "5년"; // TODO: structuredData에서 파싱

        return ApplyListResponse.builder()
                .applyId(apply.getApplyId())
                .userId(apply.getUserId())
                .jobId(apply.getJobId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .skills(skills)
                .experience(experience)
                .status(apply.getStatus().name())
                .aiScore(apply.getAiScore())
                .appliedAt(apply.getAppliedAt())
                .build();
    }

    private ApplyResponse convertToDetailResponse(Apply apply) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);
        JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);

        return ApplyResponse.builder()
                .applyId(apply.getApplyId())
                .userId(apply.getUserId())
                .jobId(apply.getJobId())
                .resumeId(apply.getResumeId())
                .coverLetterId(apply.getCoverLetterId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userPhone(user != null ? user.getPhone() : null)
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .status(apply.getStatus().name())
                .aiScore(apply.getAiScore())
                .notes(apply.getNotes())
                .appliedAt(apply.getAppliedAt())
                .reviewedAt(apply.getReviewedAt())
                .updatedAt(apply.getUpdatedAt())
                .build();
    }
}