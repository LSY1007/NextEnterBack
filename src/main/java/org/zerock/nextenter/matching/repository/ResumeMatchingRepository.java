package org.zerock.nextenter.matching.repository;

import org.zerock.nextenter.matching.entity.ResumeMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeMatchingRepository extends JpaRepository<ResumeMatching, Long> {

    List<ResumeMatching> findByResumeId(Long resumeId);

    List<ResumeMatching> findByJobId(Long jobId);

    List<ResumeMatching> findByJobIdAndGrade(Long jobId, ResumeMatching.Grade grade);

    List<ResumeMatching> findByResumeIdAndJobId(Long resumeId, Long jobId);

    // userId 필드 직접 조회 (JOIN 불필요)
    List<ResumeMatching> findByUserIdOrderByCreatedAtDesc(Long userId);
}
