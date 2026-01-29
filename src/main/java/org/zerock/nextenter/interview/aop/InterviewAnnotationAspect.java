package org.zerock.nextenter.interview.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.zerock.nextenter.interview.dto.InterviewMessageRequest;
import org.zerock.nextenter.interview.dto.InterviewQuestionResponse;
import org.zerock.nextenter.interview.service.InterviewAnnotationService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewAnnotationAspect {

    private final InterviewAnnotationService annotationService;

    /**
     * 답변 제출 후 비동기 분석 트리거
     */
    @AfterReturning(pointcut = "execution(* org.zerock.nextenter.interview.service.InterviewService.submitAnswer(..)) && args(userId, request)", returning = "response", argNames = "userId,request,response")
    public void triggerAnalysisAfterSubmit(Long userId, InterviewMessageRequest request,
            InterviewQuestionResponse response) {
        log.info("AOP: Triggering Async Analysis for Submit Answer");
        // response.currentTurn is the NEXT turn (because submitAnswer increments it).
        // The submitted answer is for the PREVIOUS turn (currentTurn - 1).
        // Wait, startInterview sets turn 1. Candidate answers.
        // submitAnswer: Saves Candidate Answer (Turn 1). Incrs Turn (Turn 2). Saves
        // Interviewer Question (Turn 2).
        // Returns currentTurn = 2.
        // So the Answer we just saved was Turn 1.

        int answerTurn = response.getCurrentTurn() - 1;
        if (answerTurn < 1)
            answerTurn = 1; // Safety

        annotationService.analyzeAndAnnotate(request.getInterviewId(), answerTurn, request.getAnswer());
    }

    /**
     * 답변 수정 후 비동기 분석 트리거
     */
    @AfterReturning(pointcut = "execution(* org.zerock.nextenter.interview.service.InterviewService.modifyAnswer(..)) && args(userId, request)", returning = "response", argNames = "userId,request,response")
    public void triggerAnalysisAfterModify(Long userId, InterviewMessageRequest request,
            InterviewQuestionResponse response) {
        log.info("AOP: Triggering Async Analysis for Modify Answer");
        // modifyAnswer does NOT increment turn necessarily?
        // Let's check modifyAnswer logic.
        // It fetches currentTurn. Updates MSG at currentTurn. Returns currentTurn.
        // So for modifyAnswer, since it doesn't increment turn, the answer turn is
        // response.getCurrentTurn().
        // BUT wait.
        // submitAnswer: Turn T => submit => Turn T+1.
        // modifyAnswer: Turn T+1 (User is at T+1, seeing Q at T+1). User wants to
        // change *previous* answer at Turn T?
        // Or User is at Turn T (question T)?
        // User answers Q(T). Status becomes ... wait.

        // Flow:
        // 1. Start -> Q1 (Turn 1). Intvw Turn=1.
        // 2. Submit A1 -> Save A1(Turn1). Incr Turn=2. Save Q2(Turn2). Return Turn=2.
        // 3. User at Turn 2 sees Q2. Wants to change A1?
        // Guideline: "If user says 'fix this', modify PREVIOUS answer".
        // So user at Turn 2 wants to modify Answer at Turn 1.
        // My modifyAnswer impl:
        // `int currentTurn = interview.getCurrentTurn();` (which is 2)
        // `candidateMsg = findBy... (turn 2, Role.CANDIDATE)` -> This is wrong!
        // If I am at Turn 2 (Q2), the *last* candidate answer was Turn 1?
        // Or is Turn 2 waiting for Candidate Answer 2?
        // "Q2 (Turn 2)" exists. User hasn't answered yet. User wants to change A1.
        // So Candidate Message at Turn 2 doesn't exist yet.
        // I should look for Candidate Message at `currentTurn - 1`.

        // Wait, let's re-verify `modifyAnswer` logic I wrote.
        // `int currentTurn = interview.getCurrentTurn();`
        // `findByInterviewIdAndTurnNumberAndRole(..., currentTurn, Role.CANDIDATE)`
        // If I am at Q2 (Turn 2), Candidate Answer at Turn 2 is NULL.
        // So `modifyAnswer` will fail with "이전 답변을 찾을 수 없습니다" if I look at
        // `currentTurn`?
        // I need to look at `currentTurn - 1` if the current state is "Waiting for
        // Answer 2".

        // User Scenario:
        // AI: "What is your weakness?" (Turn 1)
        // User: "I work too hard." (Submit) -> Turn becomes 2. AI: "Okay, next Q?"
        // (Turn 2).
        // User: "Wait, let me change that."
        // User calls modifyAnswer.
        // Interview is at Turn 2.
        // We need to modify Answer at Turn 1.

        // So `modifyAnswer` logic needs FIXING. It should modify `currentTurn - 1`.
        // UNLESS `modifyAnswer` implies we are retrying the *current* turn's answer?
        // "Dialogic Feedback Loop": "The system asks a question, user answers. AI gives
        // feedback. User refines answer."
        // If AI gave feedback and asked Next Q (Turn 2), then user refining means going
        // back to Turn 1.

        // Correction: `modifyAnswer` implementation is currently targeting
        // `currentTurn`.
        // I should change `modifyAnswer` to target `currentTurn - 1`.

        // Let's update `InterviewService` via `replace_file_content` first?
        // Or I can assume for now `modifyAnswer` logic is "User is at Turn T, Modify
        // Answer T".
        // But `submitAnswer` increments turn.
        // So `modifyAnswer` logic definitely needs to target `currentTurn - 1`.

        // I will fix `InterviewService.modifyAnswer` in the next tool call.
        // And for this Aspect, I will assume `modifyAnswer` returns the turn of the
        // modified answer?
        // `modifyAnswer` returns `InterviewQuestionResponse`.
        // If I fix `modifyAnswer` to return `currentTurn` (which is T), the modified
        // answer is T-1.
        // So for Aspect: `answerTurn = response.getCurrentTurn() - 1`.

        int answerTurn = response.getCurrentTurn() - 1;
        if (answerTurn < 1)
            answerTurn = 1;

        annotationService.analyzeAndAnnotate(request.getInterviewId(), answerTurn, request.getAnswer());
    }
}
