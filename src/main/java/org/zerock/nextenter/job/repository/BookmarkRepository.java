package org.zerock.nextenter.job.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.job.entity.Bookmark;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 특정 사용자의 특정 공고 북마크 조회
    Optional<Bookmark> findByUserIdAndJobPostingId(Long userId, Long jobPostingId);

    // 북마크 존재 여부 확인
    boolean existsByUserIdAndJobPostingId(Long userId, Long jobPostingId);

    // 사용자의 북마크 목록 (페이징)
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 사용자의 북마크한 공고 ID 목록
    @Query("SELECT b.jobPostingId FROM Bookmark b WHERE b.userId = :userId")
    List<Long> findJobPostingIdsByUserId(@Param("userId") Long userId);

    // 특정 공고의 북마크 수
    Long countByJobPostingId(Long jobPostingId);

    // 사용자의 총 북마크 수
    Long countByUserId(Long userId);

    // 사용자의 북마크 일괄 삭제 (여러 개)
    void deleteByUserIdAndJobPostingIdIn(Long userId, List<Long> jobPostingIds);
}