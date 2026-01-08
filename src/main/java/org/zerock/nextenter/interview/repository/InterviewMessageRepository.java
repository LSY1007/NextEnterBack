package org.zerock.nextenter.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.interview.entity.InterviewMessage;

import java.util.List;

@Repository
public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {

    // 특정 면접의 모든 메시지 조회 (턴 순서대로)
    List<InterviewMessage> findByInterviewIdOrderByTurnNumberAsc(Long interviewId);

    // 특정 면접의 특정 턴 메시지 조회
    List<InterviewMessage> findByInterviewIdAndTurnNumber(Long interviewId, Integer turnNumber);

    // 특정 면접의 메시지 개수
    Long countByInterviewId(Long interviewId);
}