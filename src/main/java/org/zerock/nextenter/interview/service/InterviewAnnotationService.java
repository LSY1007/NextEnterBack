package org.zerock.nextenter.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.interview.entity.InterviewAnnotation;
import org.zerock.nextenter.interview.repository.InterviewAnnotationRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewAnnotationService {

    private final InterviewAnnotationRepository annotationRepository;

    /**
     * 답변을 비동기로 분석하여 주석 저장 (Reflective Learning)
     */
    @Async
    @Transactional
    public void analyzeAndAnnotate(Long interviewId, Integer turnNumber, String answer) {
        log.info("Async Analysis Started: interviewId={}, turn={}", interviewId, turnNumber);

        try {
            // Simulate Analysis Delay
            Thread.sleep(100);

            // Rule-based Analysis (Prototype)
            double specificity = calculateSpecificity(answer);
            double starScore = checkStarCompliance(answer);
            double jobFit = 0.8; // Mock value

            String analysisContent = String.format(
                    "분석 결과: 구체성 %.1f점, STAR 구조 준수 %.1f점. " +
                            (starScore < 0.5 ? "STAR 구조(상황, 과제, 행동, 결과)를 더 명확히 하세요." : "구조적인 답변입니다."),
                    specificity * 10, starScore * 10);

            InterviewAnnotation annotation = InterviewAnnotation.builder()
                    .interviewId(interviewId)
                    .turnNumber(turnNumber)
                    .analysisContent(analysisContent)
                    .specificityScore(specificity)
                    .starComplianceScore(starScore)
                    .jobFitScore(jobFit)
                    .build();

            annotationRepository.save(annotation);
            log.info("Async Analysis Completed & Saved: annotationId={}", annotation.getAnnotationId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Analysis Failed", e);
        }
    }

    private double calculateSpecificity(String answer) {
        if (answer == null)
            return 0.0;
        int length = answer.length();
        if (length < 20)
            return 0.2;
        if (length < 50)
            return 0.5;
        if (length < 100)
            return 0.7;
        return 0.9;
    }

    private double checkStarCompliance(String answer) {
        if (answer == null)
            return 0.0;
        String lower = answer.toLowerCase();
        int score = 0;
        if (lower.contains("situation") || lower.contains("상황") || lower.contains("배경"))
            score++;
        if (lower.contains("task") || lower.contains("과제") || lower.contains("목표"))
            score++;
        if (lower.contains("action") || lower.contains("행동") || lower.contains("노력"))
            score++;
        if (lower.contains("result") || lower.contains("결과") || lower.contains("성과"))
            score++;

        return Math.min(1.0, score * 0.25 + 0.1); // Base 0.1
    }
}
