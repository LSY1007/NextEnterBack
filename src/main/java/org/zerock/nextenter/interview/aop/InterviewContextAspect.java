package org.zerock.nextenter.interview.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.zerock.nextenter.interview.dto.InterviewMessageRequest;
import org.zerock.nextenter.interview.dto.InterviewStartRequest;
import org.zerock.nextenter.interview.entity.Interview;
import org.zerock.nextenter.interview.repository.InterviewRepository;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewContextAspect {

    private final ResumeRepository resumeRepository;
    private final InterviewRepository interviewRepository;

    /**
     * 면접 시작 시 이력서 주입
     */
    @Before("execution(* org.zerock.nextenter.interview.service.InterviewService.startInterview(..)) && args(userId, request)")
    public void injectResumeForStart(Long userId, InterviewStartRequest request) {
        log.info("AOP: Injecting Resume Context for Start Interview. ResumeId={}", request.getResumeId());
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다. ResumeID: " + request.getResumeId()));
        InterviewContextHolder.setResume(resume);
    }

    /**
     * 답변 제출 시 이력서 주입
     * (기존에는 InterviewService 안에서 다시 조회했으나, AOP로 분리하여 비즈니스 로직 단순화)
     */
    @Before("execution(* org.zerock.nextenter.interview.service.InterviewService.submitAnswer(..)) && args(userId, request)")
    public void injectResumeForSubmit(Long userId, InterviewMessageRequest request) {
        log.info("AOP: Injecting Resume Context for Submit Answer. InterviewId={}", request.getInterviewId());

        // Interview 엔티티를 조회해서 ResumeId를 알아내야 함
        Interview interview = interviewRepository.findById(request.getInterviewId())
                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다."));

        Resume resume = resumeRepository.findById(interview.getResumeId())
                .orElseThrow(
                        () -> new IllegalArgumentException("이력서를 찾을 수 없습니다. ResumeID: " + interview.getResumeId()));

        InterviewContextHolder.setResume(resume);
    }

    @After("execution(* org.zerock.nextenter.interview.service.InterviewService.*(..))")
    public void clearResumeContext() {
        InterviewContextHolder.clear();
    }
}
