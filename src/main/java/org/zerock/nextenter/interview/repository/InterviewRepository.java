package org.zerock.nextenter.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.interview.entity.Interview;
import org.zerock.nextenter.interview.entity.Interview.Status;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // 사용자별 면접 목록 조회
    List<Interview> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 사용자별 + 상태별 조회
    List<Interview> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Status status);

    // 특정 면접 조회 (사용자 검증 포함)
    Optional<Interview> findByInterviewIdAndUserId(Long interviewId, Long userId);

    // 진행 중인 면접 조회
    Optional<Interview> findByUserIdAndStatus(Long userId, Status status);
}