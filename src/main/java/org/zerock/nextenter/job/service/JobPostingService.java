package org.zerock.nextenter.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.job.dto.JobPostingListResponse;
import org.zerock.nextenter.job.dto.JobPostingRequest;
import org.zerock.nextenter.job.dto.JobPostingResponse;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.BookmarkRepository;
import org.zerock.nextenter.job.repository.JobPostingRepository;

import java.util.Arrays;
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

    /**
     * 공고 목록 조회 (필터링 + 검색 + 페이징)
     */
    public Page<JobPostingListResponse> getJobPostingList(
            String jobCategories, String regions, String keyword, String status, int page, int size) {

        log.info("공고 목록 조회 - jobCategories: {}, regions: {}, keyword: {}, status: {}, page: {}", 
                jobCategories, regions, keyword, status, page);

        Pageable pageable = PageRequest.of(page, size);
        Page<JobPosting> jobPage;

        // 모든 필터가 비어있으면 전체 조회
        if ((jobCategories == null || jobCategories.isEmpty()) &&
            (regions == null || regions.isEmpty()) &&
            (keyword == null || keyword.isEmpty()) &&
            (status == null || status.isEmpty())) {
            
            jobPage = jobPostingRepository.findByStatusOrderByCreatedAtDesc(
                    JobPosting.Status.ACTIVE, pageable);
        } else {
            // 문자열을 리스트로 변환
            List<String> categoryList = (jobCategories != null && !jobCategories.isEmpty()) 
                    ? Arrays.asList(jobCategories.split(",")) 
                    : null;
            
            List<String> regionList = (regions != null && !regions.isEmpty()) 
                    ? Arrays.asList(regions.split(",")) 
                    : null;
            
            // status Enum 변환 (null이면 기본값 ACTIVE)
            JobPosting.Status statusEnum = JobPosting.Status.ACTIVE; // 기본값
            if (status != null && !status.isEmpty()) {
                try {
                    statusEnum = JobPosting.Status.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("유효하지 않은 status 값: {}", status);
                    statusEnum = JobPosting.Status.ACTIVE;
                }
            }
            
            // 필터 조건이 있으면 동적 검색 사용 (지역 LIKE 검색 포함)
            jobPage = jobPostingRepository.searchByFiltersWithRegionLike(
                    categoryList,
                    regionList,
                    keyword,
                    statusEnum,
                    pageable);
        }

        return jobPage.map(this::convertToListResponse);
    }

    /**
     * 공고 상세 조회
     */
    @Transactional
    public JobPostingResponse getJobPostingDetail(Long jobId) {
        log.info("공고 상세 조회 - jobId: {}", jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

        // 조회수 증가
        jobPostingRepository.incrementViewCount(jobId);

        return convertToResponse(jobPosting);
    }

    /**
     * 공고 등록
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
                .locationCity(request.getLocationCity()) // 시/도 정보 추가
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl()) // 썸네일 URL 추가
                .detailImageUrl(request.getDetailImageUrl()) // 상세 이미지 URL 추가
                .deadline(request.getDeadline())
                .status(request.getStatus() != null && !request.getStatus().isEmpty() ?
                        JobPosting.Status.valueOf(request.getStatus().toUpperCase()) : JobPosting.Status.ACTIVE)
                .build();

        jobPosting = jobPostingRepository.save(jobPosting);
        log.info("공고 등록 완료 - jobId: {}", jobPosting.getJobId());

        return convertToResponse(jobPosting);
    }

    /**
     * 공고 수정
     */
    @Transactional
    public JobPostingResponse updateJobPosting(
            Long jobId, JobPostingRequest request, Long companyId) {

        log.info("공고 수정 - jobId: {}, companyId: {}", jobId, companyId);

        JobPosting jobPosting = jobPostingRepository.findByJobIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "공고를 찾을 수 없거나 수정 권한이 없습니다"));

        // 수정할 필드만 업데이트
        if (request.getTitle() != null) {
            jobPosting.setTitle(request.getTitle());
        }
        if (request.getJobCategory() != null) {
            jobPosting.setJobCategory(request.getJobCategory());
        }
        if (request.getRequiredSkills() != null) {
            jobPosting.setRequiredSkills(request.getRequiredSkills());
        }
        if (request.getPreferredSkills() != null) {
            jobPosting.setPreferredSkills(request.getPreferredSkills());
        }
        if (request.getExperienceMin() != null) {
            jobPosting.setExperienceMin(request.getExperienceMin());
        }
        if (request.getExperienceMax() != null) {
            jobPosting.setExperienceMax(request.getExperienceMax());
        }
        if (request.getSalaryMin() != null) {
            jobPosting.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            jobPosting.setSalaryMax(request.getSalaryMax());
        }
        if (request.getLocation() != null) {
            jobPosting.setLocation(request.getLocation());
        }
        if (request.getLocationCity() != null) {
            jobPosting.setLocationCity(request.getLocationCity());
        }
        if (request.getDescription() != null) {
            jobPosting.setDescription(request.getDescription());
        }
        if (request.getThumbnailUrl() != null) {
            jobPosting.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getDetailImageUrl() != null) {
            jobPosting.setDetailImageUrl(request.getDetailImageUrl());
        }
        if (request.getDeadline() != null) {
            jobPosting.setDeadline(request.getDeadline());
        }
        if (request.getStatus() != null) {
            jobPosting.setStatus(JobPosting.Status.valueOf(request.getStatus()));
        }

        jobPosting = jobPostingRepository.save(jobPosting);
        log.info("공고 수정 완료 - jobId: {}", jobPosting.getJobId());

        return convertToResponse(jobPosting);
    }

    /**
     * 공고 삭제 (상태를 CLOSED로 변경)
     */
    @Transactional
    public void deleteJobPosting(Long jobId, Long companyId) {
        log.info("공고 삭제 - jobId: {}, companyId: {}", jobId, companyId);

        JobPosting jobPosting = jobPostingRepository.findByJobIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "공고를 찾을 수 없거나 삭제 권한이 없습니다"));

        // 상태를 CLOSED로 변경
        jobPosting.setStatus(JobPosting.Status.CLOSED);
        jobPostingRepository.save(jobPosting);

        log.info("공고 삭제 완료 - jobId: {}", jobId);
    }

    /**
     * 공고 상태 변경
     */
    @Transactional
    public void updateJobPostingStatus(Long jobId, Long companyId, String status) {
        log.info("공고 상태 변경 - jobId: {}, companyId: {}, status: {}", 
                jobId, companyId, status);

        JobPosting jobPosting = jobPostingRepository.findByJobIdAndCompanyId(jobId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "공고를 찾을 수 없거나 권한이 없습니다"));

        // 상태 변경
        jobPosting.setStatus(JobPosting.Status.valueOf(status.toUpperCase()));
        jobPostingRepository.save(jobPosting);

        log.info("공고 상태 변경 완료 - jobId: {}, newStatus: {}", jobId, status);
    }

    /**
     * 기업의 공고 목록 조회
     */
    public List<JobPostingListResponse> getCompanyJobPostings(Long companyId) {
        log.info("기업 공고 목록 조회 - companyId: {}", companyId);

        List<JobPosting> jobPostings = jobPostingRepository
                .findByCompanyIdOrderByCreatedAtDesc(companyId);

        return jobPostings.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    private JobPostingListResponse convertToListResponse(JobPosting jobPosting) {
        // 실제 데이터베이스에서 지원자 수 조회
        Long actualApplicantCount = applyRepository.countByJobId(jobPosting.getJobId());
        // 실제 데이터베이스에서 북마크 수 조회
        Long actualBookmarkCount = bookmarkRepository.countByJobPostingId(jobPosting.getJobId());

        // Company 정보 조회
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
                .thumbnailUrl(jobPosting.getThumbnailUrl()) // 썸네일 URL 추가
                .jobCategory(jobPosting.getJobCategory())
                .location(jobPosting.getLocation())
                .locationCity(jobPosting.getLocationCity()) // 시/도 정보 추가
                .experienceMin(jobPosting.getExperienceMin())
                .experienceMax(jobPosting.getExperienceMax())
                .salaryMin(jobPosting.getSalaryMin())
                .salaryMax(jobPosting.getSalaryMax())
                .deadline(jobPosting.getDeadline())
                .status(jobPosting.getStatus().name())
                .viewCount(jobPosting.getViewCount())
                .applicantCount(actualApplicantCount.intValue())
                .bookmarkCount(actualBookmarkCount.intValue())
                .createdAt(jobPosting.getCreatedAt())
                .build();
    }

    private JobPostingResponse convertToResponse(JobPosting jobPosting) {
        // 실제 데이터베이스에서 지원자 수 조회
        Long actualApplicantCount = applyRepository.countByJobId(jobPosting.getJobId());
        // 실제 데이터베이스에서 북마크 수 조회
        Long actualBookmarkCount = bookmarkRepository.countByJobPostingId(jobPosting.getJobId());

        log.debug("공고 상세 변환 - jobId: {}, 실제 지원자 수: {}, 실제 북마크 수: {}",
                jobPosting.getJobId(), actualApplicantCount, actualBookmarkCount);

        // Company 정보 조회
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
                .locationCity(jobPosting.getLocationCity()) // 시/도 정보 추가
                .description(jobPosting.getDescription())
                .thumbnailUrl(jobPosting.getThumbnailUrl()) // 썸네일 URL 추가
                .detailImageUrl(jobPosting.getDetailImageUrl()) // 상세 이미지 URL 추가
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