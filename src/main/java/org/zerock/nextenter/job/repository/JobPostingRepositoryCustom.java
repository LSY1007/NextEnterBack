package org.zerock.nextenter.job.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zerock.nextenter.job.entity.JobPosting;

import java.util.List;

public interface JobPostingRepositoryCustom {
    Page<JobPosting> searchByFiltersWithRegionLike(
            List<String> categoryList,
            List<String> regionList,
            String keyword,
            JobPosting.Status statusEnum,
            Pageable pageable
    );
}
