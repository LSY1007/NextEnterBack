package org.zerock.nextenter.recommendation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.recommendation.entity.JobRecommendation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {

    // 사용자별 추천 히스토리 (페이징)
    Page<JobRecommendation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 특정 이력서로 받은 추천 목록
    List<JobRecommendation> findByUserIdAndResumeIdOrderByCreatedAtDesc(Long userId, Long resumeId);

    // 최근 추천 내역 (N개)
    List<JobRecommendation> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 기간 내 추천 횟수
    Long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}