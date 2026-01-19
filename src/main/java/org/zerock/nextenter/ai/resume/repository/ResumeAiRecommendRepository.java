package org.zerock.nextenter.ai.resume.repository;

import org.zerock.nextenter.ai.resume.entity.ResumeAiRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeAiRecommendRepository extends JpaRepository<ResumeAiRecommend, Long> {

    // 특정 이력서의 추천 이력 조회 (최신순)
    List<ResumeAiRecommend> findByResumeIdOrderByCreatedAtDesc(Long resumeId);

    // 특정 사용자의 추천 이력 조회 (최신순)
    List<ResumeAiRecommend> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 이력서의 최신 추천 결과 1개
    Optional<ResumeAiRecommend> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);

    // 특정 사용자의 최신 추천 결과 1개
    Optional<ResumeAiRecommend> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
