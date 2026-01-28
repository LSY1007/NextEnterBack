package org.zerock.nextenter.resume.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.resume.entity.Resume;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 사용자별 이력서 목록 (삭제되지 않은 것만, 최신순)
    List<Resume> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    // ID와 사용자 ID로 조회 (본인 확인용)
    Optional<Resume> findByResumeIdAndUserIdAndDeletedAtIsNull(Long resumeId, Long userId);

    // 상태별 조회
    List<Resume> findByUserIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long userId, String status);

    // 사용자의 이력서 개수
    long countByUserIdAndDeletedAtIsNull(Long userId);

    // 메인 이력서 조회
    Optional<Resume> findByUserIdAndIsMainTrueAndDeletedAtIsNull(Long userId);

    // 공개 이력서 조회
    List<Resume> findByVisibilityAndDeletedAtIsNullOrderByCreatedAtDesc(
            Resume.Visibility visibility);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Resume r SET r.viewCount = r.viewCount + 1 WHERE r.resumeId = :resumeId")
    void incrementViewCount(@Param("resumeId") Long resumeId);

    /**
     * 인재 검색 - 공개된 이력서 중 검색 (페이징)
     * skills, jobCategory, experiences, careers 등 여러 필드를 검색합니다.
     */
    @Query("SELECT r FROM Resume r WHERE r.visibility = 'PUBLIC' AND r.deletedAt IS NULL " +
            "AND (:jobCategory IS NULL OR :jobCategory = '' OR r.jobCategory = :jobCategory) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(r.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.jobCategory) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.experiences) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.careers) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.certificates) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.educations) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY r.createdAt DESC")
    Page<Resume> searchTalents(
            @Param("jobCategory") String jobCategory,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 특정 직무의 공개 이력서 개수 조회
     */
    @Query("SELECT COUNT(r) FROM Resume r WHERE r.visibility = 'PUBLIC' " +
            "AND r.deletedAt IS NULL AND r.jobCategory = :jobCategory")
    long countPublicResumesByJobCategory(@Param("jobCategory") String jobCategory);

    /**
     * 경력 년수별 공개 이력서 조회 (careers JSON 필드를 이용)
     */
    @Query("SELECT r FROM Resume r WHERE r.visibility = 'PUBLIC' AND r.deletedAt IS NULL " +
            "AND r.careers IS NOT NULL AND r.careers != '' " +
            "ORDER BY r.createdAt DESC")
    Page<Resume> findPublicResumesWithCareers(Pageable pageable);
}