package org.zerock.nextenter.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.job.dto.JobPostingListResponse;
import org.zerock.nextenter.job.dto.JobPostingPageResponse;
import org.zerock.nextenter.job.dto.JobPostingRequest;
import org.zerock.nextenter.job.dto.JobPostingResponse;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.BookmarkRepository;
import org.zerock.nextenter.job.repository.JobPostingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final ApplyRepository applyRepository;
    private final BookmarkRepository bookmarkRepository;
    private final org.zerock.nextenter.company.repository.CompanyRepository companyRepository;
    private final JobPostingCacheService jobPostingCacheService;

    /**
     * 공고 목록 조회 - CacheService를 통해 Redis 캐싱
     */
    public Page<JobPostingListResponse> getJobPostingList(
            String jobCategories, String regions, String keyword, String status, int page, int size) {

        JobPostingPageResponse cached = jobPostingCacheService.getCachedJobList(
                jobCategories, regions, keyword, status, page, size);

        return new PageImpl<>(
                cached.getContent(),
                PageRequest.of(cached.getPage(), cached.getSize()),
                cached.getTotalElements()
        );
    }

    /**
     * 공고 상세 조회 - Redis 캐싱
     */
    @Cacheable(value = "jobDetail", key = "#jobId")
    @Transactional
    public JobPostingResponse getJobPostingDetail(Long jobId) {
        log.info("[CACHE MISS] 공고 상세 DB 조회 - jobId: {}", jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

        jobPostingRepository.incrementViewCount(jobId);

        return convertToResponse(jobPosting);
    }

    /**
     * 공고 등록 - 캐시 초기화
     */
    @Transactional
    public JobPostingResponse createJobPosting(JobPostingRequest request, Long companyId) {
        log.info("공고 등록 - companyId: {}, title: {}", companyId, request.getTitle());

        JobPosting jobPosting = JobPosting.builder()
                .companyId(companyId)
                .title(request.getTitle())
                .jobCategory(request.getJobCategory())
                .requiredSkills(request.getRequiredSkills())
                .preferredSkills(request.getPreferredSkills())
                .experienceMin(request.getExperienceMin())
                .experienceMax(request.getExperienceMax())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .location(request.getLocation())
                .locationCity(request.getLocationCity())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .detailImageUrl(request.getDetailImageUrl())
                .deadline(request.getDeadline())
                .status(request.getStatus() != null && !request.getStatus().isEmpty() ?
                        JobPosting.Status.valueOf(request.getStatus().toUpperCase()) : JobPosting.Status.ACTIVE)
                .build();

        jobPosting = jobPostingRepository.save(jobPosting);
        jobPostingCacheService.evictAllCaches();
        log.info("공고 등록 완료 - jobId: {}", jobPosting.getJobId());

        return convertToResponse(jobPosting);
    }

    /**
     * 공고 수정 - 캐시 초기화
     */
    @Transactional
    public JobPostingResponse updateJobPosting(Long jobId, JobPostingRequest request, Long companyId) {
        log.info("공고 수정 - jobId: {}, companyId: {}", jobId, companyId);

        JobPosting jobPosting = jobPostingRepository.findByJobIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없거나 수정 권한이 없습니다"));

        if (request.getTitle() != null) jobPosting.setTitle(request.getTitle());
        if (request.getJobCategory() != null) jobPosting.setJobCategory(request.getJobCategory());
        if (request.getRequiredSkills() != null) jobPosting.setRequiredSkills(request.getRequiredSkills());
        if (request.getPreferredSkills() != null) jobPosting.setPreferredSkills(request.getPreferredSkills());
        if (request.getExperienceMin() != null) jobPosting.setExperienceMin(request.getExperienceMin());
        if (request.getExperienceMax() != null) jobPosting.setExperienceMax(request.getExperienceMax());
        if (request.getSalaryMin() != null) jobPosting.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) jobPosting.setSalaryMax(request.getSalaryMax());
        if (request.getLocation() != null) jobPosting.setLocation(request.getLocation());
        if (request.getLocationCity() != null) jobPosting.setLocationCity(request.getLocationCity());
        if (request.getDescription() != null) jobPosting.setDescription(request.getDescription());
        if (request.getThumbnailUrl() != null) jobPosting.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getDetailImageUrl() != null) jobPosting.setDetailImageUrl(request.getDetailImageUrl());
        if (request.getDeadline() != null) jobPosting.setDeadline(request.getDeadline());
        if (request.getStatus() != null) jobPosting.setStatus(JobPosting.Status.valueOf(request.getStatus()));

        jobPosting = jobPostingRepository.save(jobPosting);
        jobPostingCacheService.evictAllCaches();
        log.info("공고 수정 완료 - jobId: {}", jobPosting.getJobId());

        return convertToResponse(jobPosting);
    }

    /**
     * 공고 삭제 - 캐시 초기화
     */
    @Transactional
    public void deleteJobPosting(Long jobId, Long companyId) {
        log.info("공고 삭제 - jobId: {}, companyId: {}", jobId, companyId);

        JobPosting jobPosting = jobPostingRepository.findByJobIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없거나 삭제 권한이 없습니다"));

        jobPosting.setStatus(JobPosting.Status.CLOSED);
        jobPostingRepository.save(jobPosting);
        jobPostingCacheService.evictAllCaches();
        log.info("공고 삭제 완료 - jobId: {}", jobId);
    }

    /**
     * 공고 상태 변경 - 캐시 초기화
     */
    @Transactional
    public void updateJobPostingStatus(Long jobId, Long companyId, String status) {
        log.info("공고 상태 변경 - jobId: {}, companyId: {}, status: {}", jobId, companyId, status);

        JobPosting jobPosting = jobPostingRepository.findByJobIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없거나 권한이 없습니다"));

        jobPosting.setStatus(JobPosting.Status.valueOf(status.toUpperCase()));
        jobPostingRepository.save(jobPosting);
        jobPostingCacheService.evictAllCaches();
        log.info("공고 상태 변경 완료 - jobId: {}, newStatus: {}", jobId, status);
    }

    /**
     * 기업의 공고 목록 조회
     */
    public List<JobPostingListResponse> getCompanyJobPostings(Long companyId) {
        log.info("기업 공고 목록 조회 - companyId: {}", companyId);

        return jobPostingRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    private JobPostingListResponse convertToListResponse(JobPosting jobPosting) {
        Long actualApplicantCount = applyRepository.countByJobId(jobPosting.getJobId());
        Long actualBookmarkCount = bookmarkRepository.countByJobPostingId(jobPosting.getJobId());

        String companyName = "회사명";
        String logoUrl = null;
        try {
            var company = companyRepository.findById(jobPosting.getCompanyId());
            if (company.isPresent()) {
                companyName = company.get().getCompanyName();
                logoUrl = company.get().getLogoUrl();
            }
        } catch (Exception e) {
            log.warn("Company 정보 조회 실패 - companyId: {}", jobPosting.getCompanyId());
        }

        return JobPostingListResponse.builder()
                .jobId(jobPosting.getJobId())
                .companyId(jobPosting.getCompanyId())
                .title(jobPosting.getTitle())
                .companyName(companyName)
                .logoUrl(logoUrl)
                .thumbnailUrl(jobPosting.getThumbnailUrl())
                .detailImageUrl(jobPosting.getDetailImageUrl())
                .jobCategory(jobPosting.getJobCategory())
                .location(jobPosting.getLocation())
                .locationCity(jobPosting.getLocationCity())
                .experienceMin(jobPosting.getExperienceMin())
                .experienceMax(jobPosting.getExperienceMax())
                .salaryMin(jobPosting.getSalaryMin())
                .salaryMax(jobPosting.getSalaryMax())
                .description(jobPosting.getDescription())
                .deadline(jobPosting.getDeadline())
                .status(jobPosting.getStatus().name())
                .viewCount(jobPosting.getViewCount())
                .applicantCount(actualApplicantCount.intValue())
                .bookmarkCount(actualBookmarkCount.intValue())
                .createdAt(jobPosting.getCreatedAt())
                .build();
    }

    private JobPostingResponse convertToResponse(JobPosting jobPosting) {
        Long actualApplicantCount = applyRepository.countByJobId(jobPosting.getJobId());
        Long actualBookmarkCount = bookmarkRepository.countByJobPostingId(jobPosting.getJobId());

        String companyName = "회사명";
        String logoUrl = null;
        try {
            var company = companyRepository.findById(jobPosting.getCompanyId());
            if (company.isPresent()) {
                companyName = company.get().getCompanyName();
                logoUrl = company.get().getLogoUrl();
            }
        } catch (Exception e) {
            log.warn("Company 정보 조회 실패 - companyId: {}", jobPosting.getCompanyId());
        }

        return JobPostingResponse.builder()
                .jobId(jobPosting.getJobId())
                .companyId(jobPosting.getCompanyId())
                .companyName(companyName)
                .logoUrl(logoUrl)
                .title(jobPosting.getTitle())
                .jobCategory(jobPosting.getJobCategory())
                .requiredSkills(jobPosting.getRequiredSkills())
                .preferredSkills(jobPosting.getPreferredSkills())
                .experienceMin(jobPosting.getExperienceMin())
                .experienceMax(jobPosting.getExperienceMax())
                .salaryMin(jobPosting.getSalaryMin())
                .salaryMax(jobPosting.getSalaryMax())
                .location(jobPosting.getLocation())
                .locationCity(jobPosting.getLocationCity())
                .description(jobPosting.getDescription())
                .thumbnailUrl(jobPosting.getThumbnailUrl())
                .detailImageUrl(jobPosting.getDetailImageUrl())
                .deadline(jobPosting.getDeadline())
                .status(jobPosting.getStatus().name())
                .viewCount(jobPosting.getViewCount())
                .applicantCount(actualApplicantCount.intValue())
                .bookmarkCount(actualBookmarkCount.intValue())
                .createdAt(jobPosting.getCreatedAt())
                .updatedAt(jobPosting.getUpdatedAt())
                .build();
    }
}
