package org.zerock.nextenter.matching.repository;

import org.zerock.nextenter.matching.entity.ResumeMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeMatchingRepository extends JpaRepository<ResumeMatching, Long> {

    // 특정 이력서의 모든 매칭 결과 조회
    List<ResumeMatching> findByResumeId(Long resumeId);

    // 특정 공고의 모든 매칭 결과 조회
    List<ResumeMatching> findByJobId(Long jobId);

    // 특정 공고의 특정 등급 매칭 결과 조회
    List<ResumeMatching> findByJobIdAndGrade(Long jobId, ResumeMatching.Grade grade);

    // 특정 이력서와 특정 공고의 매칭 결과 조회
    List<ResumeMatching> findByResumeIdAndJobId(Long resumeId, Long jobId);

    // ✅ 추가: 특정 사용자의 모든 매칭 히스토리 조회
    @Query("SELECT rm FROM ResumeMatching rm " +
            "JOIN Resume r ON rm.resumeId = r.resumeId " +
            "WHERE r.userId = :userId " +
            "ORDER BY rm.createdAt DESC")
    List<ResumeMatching> findByUserId(@Param("userId") Long userId);
}