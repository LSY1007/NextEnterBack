package org.zerock.nextenter.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.interview.entity.InterviewAnnotation;

import java.util.List;

@Repository
public interface InterviewAnnotationRepository extends JpaRepository<InterviewAnnotation, Long> {
    List<InterviewAnnotation> findByInterviewId(Long interviewId);
}
