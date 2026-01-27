package org.zerock.nextenter.job.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.job.entity.JobPosting;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JobPostingRepositoryCustom {

    // 전체 목록 조회 (페이징)
    Page<JobPosting> findByStatusOrderByCreatedAtDesc(
            JobPosting.Status status, Pageable pageable);

    // 직무 카테고리로 필터링 (페이징)
    Page<JobPosting> findByJobCategoryAndStatusOrderByCreatedAtDesc(
            String jobCategory, JobPosting.Status status, Pageable pageable);

    // 키워드 검색 (제목 또는 설명에 포함) (페이징)
    @Query("SELECT j FROM JobPosting j WHERE " +
            "(j.title LIKE %:keyword% OR j.description LIKE %:keyword%) " +
            "AND j.status = :status ORDER BY j.createdAt DESC")
    Page<JobPosting> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("status") JobPosting.Status status,
            Pageable pageable);

    // 직무 + 키워드 검색 (페이징)
    @Query("SELECT j FROM JobPosting j WHERE " +
            "j.jobCategory = :category AND " +
            "(j.title LIKE %:keyword% OR j.description LIKE %:keyword%) " +
            "AND j.status = :status ORDER BY j.createdAt DESC")
    Page<JobPosting> searchByCategoryAndKeyword(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("status") JobPosting.Status status,
            Pageable pageable);

    // 기업별 공고 조회
    List<JobPosting> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // 기업별 공고 조회 (상태별)
    List<JobPosting> findByCompanyIdAndStatusOrderByCreatedAtDesc(
            Long companyId, JobPosting.Status status);

    // 공고 상세 조회 (company_id 확인용)
    Optional<JobPosting> findByJobIdAndCompanyId(Long jobId, Long companyId);

    // 조회수 증가
    @Modifying
    @Query("UPDATE JobPosting j SET j.viewCount = j.viewCount + 1 WHERE j.jobId = :jobId")
    void incrementViewCount(@Param("jobId") Long jobId);

    // 지원자 수 증가
    @Modifying
    @Query("UPDATE JobPosting j SET j.applicantCount = j.applicantCount + 1 WHERE j.jobId = :jobId")
    void incrementApplicantCount(@Param("jobId") Long jobId);

    // 북마크 수 증가
    @Modifying
    @Query("UPDATE JobPosting j SET j.bookmarkCount = j.bookmarkCount + 1 WHERE j.jobId = :jobId")
    void incrementBookmarkCount(@Param("jobId") Long jobId);

    // 북마크 수 감소
    @Modifying
    @Query("UPDATE JobPosting j SET j.bookmarkCount = CASE WHEN j.bookmarkCount > 0 THEN j.bookmarkCount - 1 ELSE 0 END WHERE j.jobId = :jobId")
    void decrementBookmarkCount(@Param("jobId") Long jobId);

    // 동적 필터 검색 (여러 조건 조합)
    @Query("SELECT j FROM JobPosting j WHERE " +
            "(:jobCategories IS NULL OR :jobCategories = '' OR j.jobCategory IN :categoryList) AND " +
            "(:regions IS NULL OR :regions = '' OR j.location IN :regionList) AND " +
            "(:keyword IS NULL OR :keyword = '' OR j.title LIKE %:keyword% OR j.description LIKE %:keyword%) AND " +
            "j.status = :statusEnum " +  // statusEnum은 Service에서 항상 설정됨 (기본값 ACTIVE)
            "ORDER BY j.createdAt DESC")
    Page<JobPosting> searchByFilters(
            @Param("jobCategories") String jobCategories,
            @Param("categoryList") List<String> categoryList,
            @Param("regions") String regions,
            @Param("regionList") List<String> regionList,
            @Param("keyword") String keyword,
            @Param("statusEnum") JobPosting.Status statusEnum,  // status 대신 statusEnum만 사용
            Pageable pageable);
}