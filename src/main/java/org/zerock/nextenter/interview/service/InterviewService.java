package org.zerock.nextenter.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.interview.dto.*;
import org.zerock.nextenter.interview.entity.Interview;
import org.zerock.nextenter.interview.entity.Interview.Difficulty;
import org.zerock.nextenter.interview.entity.Interview.Status;
import org.zerock.nextenter.interview.entity.InterviewMessage;
import org.zerock.nextenter.interview.entity.InterviewMessage.Role;
import org.zerock.nextenter.interview.repository.InterviewMessageRepository;
import org.zerock.nextenter.interview.repository.InterviewRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewMessageRepository interviewMessageRepository;
    // TODO: AI 클라이언트 추가
    // private final AiInterviewClient aiInterviewClient;

    /**
     * 면접 시작
     */
    @Transactional
    public InterviewQuestionResponse startInterview(Long userId, InterviewStartRequest request) {
        // 진행 중인 면접이 있는지 확인
        interviewRepository.findByUserIdAndStatus(userId, Status.IN_PROGRESS)
                .ifPresent(existing -> {
                    throw new IllegalStateException("이미 진행 중인 면접이 있습니다. 먼저 완료하거나 취소해주세요.");
                });

        // Difficulty 유효성 검증
        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(request.getDifficulty().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "유효하지 않은 난이도입니다. JUNIOR 또는 SENIOR만 가능합니다. 입력값: " + request.getDifficulty()
            );
        }

        // 면접 세션 생성
        Interview interview = Interview.builder()
                .userId(userId)
                .resumeId(request.getResumeId())
                .jobCategory(request.getJobCategory())
                .difficulty(difficulty)
                .totalTurns(request.getTotalTurns() != null ? request.getTotalTurns() : 5)
                .currentTurn(0)
                .status(Status.IN_PROGRESS)
                .build();

        interviewRepository.save(interview);
        log.info("면접 시작: interviewId={}, userId={}, jobCategory={}",
                interview.getInterviewId(), userId, request.getJobCategory());

        // TODO: AI에게 첫 질문 요청
        String firstQuestion = generateFirstQuestion(interview);

        // 첫 질문 저장
        InterviewMessage questionMessage = InterviewMessage.builder()
                .interviewId(interview.getInterviewId())
                .turnNumber(1)
                .role(Role.INTERVIEWER)
                .message(firstQuestion)
                .build();
        interviewMessageRepository.save(questionMessage);

        interview.incrementTurn();
        interviewRepository.save(interview);

        return InterviewQuestionResponse.builder()
                .interviewId(interview.getInterviewId())
                .currentTurn(interview.getCurrentTurn())
                .question(firstQuestion)
                .isCompleted(false)
                .build();
    }

    /**
     * 답변 제출 및 다음 질문 받기
     */
    @Transactional
    public InterviewQuestionResponse submitAnswer(Long userId, InterviewMessageRequest request) {
        Interview interview = interviewRepository.findByInterviewIdAndUserId(
                        request.getInterviewId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다"));

        if (interview.getStatus() != Status.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 면접이 아닙니다");
        }

        // 답변 저장
        InterviewMessage answerMessage = InterviewMessage.builder()
                .interviewId(interview.getInterviewId())
                .turnNumber(interview.getCurrentTurn())
                .role(Role.CANDIDATE)
                .message(request.getAnswer())
                .build();
        interviewMessageRepository.save(answerMessage);

        // 면접이 완료되었는지 확인
        if (interview.getCurrentTurn() >= interview.getTotalTurns()) {
            // TODO: AI에게 최종 평가 요청
            String finalFeedback = generateFinalFeedback(interview);
            Integer finalScore = calculateFinalScore(interview);

            interview.completeInterview(finalScore, finalFeedback);
            interviewRepository.save(interview);

            return InterviewQuestionResponse.builder()
                    .interviewId(interview.getInterviewId())
                    .currentTurn(interview.getCurrentTurn())
                    .isCompleted(true)
                    .finalScore(finalScore)
                    .finalFeedback(finalFeedback)
                    .build();
        }

        // TODO: AI에게 다음 질문 요청
        String nextQuestion = generateNextQuestion(interview, request.getAnswer());

        // 다음 질문 저장
        interview.incrementTurn();
        InterviewMessage questionMessage = InterviewMessage.builder()
                .interviewId(interview.getInterviewId())
                .turnNumber(interview.getCurrentTurn())
                .role(Role.INTERVIEWER)
                .message(nextQuestion)
                .build();
        interviewMessageRepository.save(questionMessage);
        interviewRepository.save(interview);

        return InterviewQuestionResponse.builder()
                .interviewId(interview.getInterviewId())
                .currentTurn(interview.getCurrentTurn())
                .question(nextQuestion)
                .isCompleted(false)
                .build();
    }

    /**
     * 면접 결과 조회
     */
    public InterviewResultDTO getInterviewResult(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findByInterviewIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다"));

        List<InterviewMessage> messages = interviewMessageRepository
                .findByInterviewIdOrderByTurnNumberAsc(interviewId);

        return InterviewResultDTO.builder()
                .interviewId(interview.getInterviewId())
                .userId(interview.getUserId())
                .resumeId(interview.getResumeId())
                .jobCategory(interview.getJobCategory())
                .difficulty(interview.getDifficulty().name())
                .totalTurns(interview.getTotalTurns())
                .currentTurn(interview.getCurrentTurn())
                .status(interview.getStatus().name())
                .finalScore(interview.getFinalScore())
                .finalFeedback(interview.getFinalFeedback())
                .createdAt(interview.getCreatedAt())
                .completedAt(interview.getCompletedAt())
                .messages(messages.stream()
                        .map(msg -> InterviewResultDTO.MessageDto.builder()
                                .messageId(msg.getMessageId())
                                .turnNumber(msg.getTurnNumber())
                                .role(msg.getRole().name())
                                .message(msg.getMessage())
                                .createdAt(msg.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 면접 히스토리 목록 조회
     */
    public List<InterviewHistoryDTO> getInterviewHistory(Long userId) {
        List<Interview> interviews = interviewRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return interviews.stream()
                .map(interview -> InterviewHistoryDTO.builder()
                        .interviewId(interview.getInterviewId())
                        .jobCategory(interview.getJobCategory())
                        .difficulty(interview.getDifficulty().name())
                        .totalTurns(interview.getTotalTurns())
                        .currentTurn(interview.getCurrentTurn())
                        .status(interview.getStatus().name())
                        .finalScore(interview.getFinalScore())
                        .createdAt(interview.getCreatedAt())
                        .completedAt(interview.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 면접 취소
     */
    @Transactional
    public void cancelInterview(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findByInterviewIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다"));

        if (interview.getStatus() != Status.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 면접만 취소할 수 있습니다");
        }

        interview.cancelInterview();
        interviewRepository.save(interview);
        log.info("면접 취소: interviewId={}, userId={}", interviewId, userId);
    }

    // ===== Private Methods (TODO: AI 연동) =====

    private String generateFirstQuestion(Interview interview) {
        // TODO: AI 클라이언트로 첫 질문 생성
        // String question = aiInterviewClient.generateFirstQuestion(interview);
        return "자기소개를 간단히 해주세요.";
    }

    private String generateNextQuestion(Interview interview, String previousAnswer) {
        // TODO: AI 클라이언트로 다음 질문 생성
        // String question = aiInterviewClient.generateNextQuestion(interview, previousAnswer);
        return "지원 동기를 말씀해주세요.";
    }

    private String generateFinalFeedback(Interview interview) {
        // TODO: AI 클라이언트로 최종 피드백 생성
        // String feedback = aiInterviewClient.generateFinalFeedback(interview);
        return "전반적으로 좋은 답변이었습니다.";
    }

    private Integer calculateFinalScore(Interview interview) {
        // TODO: AI 클라이언트로 점수 계산
        // Integer score = aiInterviewClient.calculateScore(interview);
        return 85;
    }
}