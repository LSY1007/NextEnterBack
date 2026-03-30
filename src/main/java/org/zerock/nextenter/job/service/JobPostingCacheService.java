package org.zerock.nextenter.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.job.dto.JobPostingListResponse;
import org.zerock.nextenter.job.dto.JobPostingPageResponse;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.BookmarkRepository;
import org.zerock.nextenter.job.repository.JobPostingRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobPostingCacheService {

    private final JobPostingRepository jobPostingRepository;
    private final ApplyRepository applyRepository;
    private final BookmarkRepository bookmarkRepository;
    private final org.zerock.nextenter.company.repository.CompanyRepository companyRepository;

    @Cacheable(value = "jobList", key = "#jobCategories + '_' + #regions + '_' + #keyword + '_' + #status + '_' + #page + '_' + #size")
    public JobPostingPageResponse getCachedJobList(
            String jobCategories, String regions, String keyword, String status, int page, int size) {

        log.info("[CACHE MISS] 공고 목록 DB 조회 - status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size);

        List<String> categoryList = (jobCategories != null && !jobCategories.isEmpty())
                ? Arrays.asList(jobCategories.split(",")) : null;

        List<String> regionList = (regions != null && !regions.isEmpty())
                ? Arrays.asList(regions.split(",")) : null;

        JobPosting.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = JobPosting.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("유효하지 않은 status 값: {}", status);
            }
        }

        Page<JobPosting> jobPage = jobPostingRepository.searchByFiltersWithRegionLike(
                categoryList, regionList, keyword, statusEnum, pageable);

        List<JobPostingListResponse> content = jobPage.getContent().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return JobPostingPageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(jobPage.getTotalElements())
                .totalPages(jobPage.getTotalPages())
                .last(jobPage.isLast())
                .build();
    }

    @CacheEvict(value = {"jobList", "jobDetail"}, allEntries = true)
    public void evictAllCaches() {
        log.info("[CACHE EVICT] 공고 캐시 전체 초기화");
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
}
