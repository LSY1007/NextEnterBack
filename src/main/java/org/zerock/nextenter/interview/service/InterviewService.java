package org.zerock.nextenter.interview.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.interview.client.AiInterviewClient;
import org.zerock.nextenter.interview.client.AiInterviewClient.AiInterviewRequest;
import org.zerock.nextenter.interview.client.AiInterviewClient.AiInterviewResponse;
import org.zerock.nextenter.interview.dto.*;
import org.zerock.nextenter.interview.entity.Interview;
import org.zerock.nextenter.interview.entity.Interview.Difficulty;
import org.zerock.nextenter.interview.entity.Interview.Status;
import org.zerock.nextenter.interview.entity.InterviewMessage;
import org.zerock.nextenter.interview.entity.InterviewMessage.Role;
import org.zerock.nextenter.interview.repository.InterviewMessageRepository;
import org.zerock.nextenter.interview.repository.InterviewRepository;
import org.zerock.nextenter.resume.entity.Portfolio;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.PortfolioRepository;
import org.zerock.nextenter.resume.repository.ResumeRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InterviewService {

        private final InterviewRepository interviewRepository;
        private final InterviewMessageRepository interviewMessageRepository;
        private final PortfolioRepository portfolioRepository;
        private final ResumeRepository resumeRepository;
        private final AiInterviewClient aiInterviewClient;
        private final ObjectMapper objectMapper;

        /**
         * 면접 시작
         */
        @Transactional
        public InterviewQuestionResponse startInterview(Long userId, InterviewStartRequest request) {
                // 1. 진행 중인 면접이 있는지 확인
                interviewRepository.findByUserIdAndStatus(userId, Status.IN_PROGRESS)
                                .ifPresent(existing -> {
                                        throw new IllegalStateException("이미 진행 중인 면접이 있습니다. 먼저 완료하거나 취소해주세요.");
                                });

                // 2. 이력서 조회 (Direct Lookup)
                Resume resume = resumeRepository.findById(request.getResumeId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "이력서를 찾을 수 없습니다. ID: " + request.getResumeId()));

                log.info("Resume Found: resumeId={}", resume.getResumeId());

                // 3. Difficulty 유효성 검증
                Difficulty difficulty;
                try {
                        difficulty = Difficulty.valueOf(request.getDifficulty().toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                                        "유효하지 않은 난이도입니다. JUNIOR 또는 SENIOR만 가능합니다. 입력값: " + request.getDifficulty());
                }

                // 직무 정규화 (Data Analyst -> AI/LLM Engineer)
                String normalizedJobCategory = org.zerock.nextenter.common.constants.JobConstants
                                .normalize(request.getJobCategory());

                // 4. 면접 세션 생성
                Interview interview = Interview.builder()
                                .userId(userId)
                                .resumeId(request.getResumeId())
                                .jobCategory(normalizedJobCategory) // Normalized
                                .difficulty(difficulty)
                                .totalTurns(request.getTotalTurns() != null ? request.getTotalTurns() : 5)
                                .currentTurn(0)
                                .status(Status.IN_PROGRESS)
                                .build();

                interviewRepository.save(interview);
                log.info("면접 세션 생성 완료: interviewId={}, userId={}, jobCategory={}",
                                interview.getInterviewId(), userId, normalizedJobCategory);

                // 5. Context 구성 (DB 조회)
                Map<String, Object> resumeContent = buildResumeContent(resume);

                List<String> portfolioFiles = portfolioRepository
                                .findByResumeIdOrderByDisplayOrder(request.getResumeId())
                                .stream()
                                .map(Portfolio::getFilePath)
                                .collect(Collectors.toList());

                // 6. AI에게 첫 질문 요청
                AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                                .id(userId.toString())
                                .targetRole(normalizedJobCategory) // Normalized
                                .resumeContent(resumeContent)
                                .lastAnswer(null) // 첫 질문이므로 null
                                .portfolioFiles(portfolioFiles)
                                .build();

                log.info("AI Server 요청 준비: targetRole={}, resumeId={}", aiRequest.getTargetRole(),
                                resume.getResumeId());

                AiInterviewResponse aiResponse;
                try {
                        aiResponse = aiInterviewClient.getNextQuestion(aiRequest);
                        log.info("AI Server 응답 성공");
                } catch (Exception e) {
                        log.error("AI Server 연동 실패", e);
                        throw new RuntimeException("AI 서버 연동 실패: " + e.getMessage());
                }

                String firstQuestion = aiResponse.getRealtime().getNextQuestion();

                // 7. 첫 질문 저장
                InterviewMessage questionMessage = InterviewMessage.builder()
                                .interviewId(interview.getInterviewId())
                                .turnNumber(1)
                                .role(Role.INTERVIEWER)
                                .message(firstQuestion)
                                .build();
                interviewMessageRepository.save(questionMessage);

                interview.incrementTurn();
                interviewRepository.save(interview);
                log.info("면접 시작 프로세스 완료: interviewId={}, firstQuestion={}", interview.getInterviewId(), firstQuestion);

                return InterviewQuestionResponse.builder()
                                .interviewId(interview.getInterviewId())
                                .currentTurn(interview.getCurrentTurn())
                                .question(firstQuestion)
                                .isCompleted(false)
                                .reactionType(aiResponse.getRealtime().getReaction() != null
                                                ? aiResponse.getRealtime().getReaction().getType()
                                                : null)
                                .reactionText(aiResponse.getRealtime().getReaction() != null
                                                ? aiResponse.getRealtime().getReaction().getText()
                                                : null)
                                .aiSystemReport(aiResponse.getRealtime().getReport())
                                .requestedEvidence(aiResponse.getRealtime().getRequestedEvidence())
                                .probeGoal(aiResponse.getRealtime().getProbeGoal())
                                .build();
        }

        /**
         * 답변 제출 및 다음 질문 받기
         */
        @Transactional
        public InterviewQuestionResponse submitAnswer(Long userId, InterviewMessageRequest request) {
                // 1. 면접 세션 조회
                Interview interview = interviewRepository.findByInterviewIdAndUserId(
                                request.getInterviewId(), userId)
                                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다"));

                if (interview.getStatus() != Status.IN_PROGRESS) {
                        throw new IllegalStateException("진행 중인 면접이 아닙니다");
                }

                // 2. 이력서 조회
                Resume resume = resumeRepository.findById(interview.getResumeId())
                                .orElseThrow(() -> new IllegalStateException("이력서 정보를 찾을 수 없습니다."));

                // 3. 사용자 답변 저장
                InterviewMessage answerMessage = InterviewMessage.builder()
                                .interviewId(interview.getInterviewId())
                                .turnNumber(interview.getCurrentTurn())
                                .role(Role.CANDIDATE)
                                .message(request.getAnswer())
                                .build();
                interviewMessageRepository.save(answerMessage);

                // 4. AI에게 답변 전송
                Map<String, Object> resumeContent = buildResumeContent(resume);
                List<String> portfolioFiles = portfolioRepository
                                .findByResumeIdOrderByDisplayOrder(resume.getResumeId())
                                .stream()
                                .map(Portfolio::getFilePath)
                                .collect(Collectors.toList());

                // 직무 정규화 (DB에 예전 값이 있을 수 있으므로)
                String normalizedJobCategory = org.zerock.nextenter.common.constants.JobConstants
                                .normalize(interview.getJobCategory());

                AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                                .id(userId.toString())
                                .targetRole(normalizedJobCategory) // Normalized
                                .resumeContent(resumeContent)
                                .lastAnswer(request.getAnswer())
                                .portfolioFiles(portfolioFiles)
                                .build();

                AiInterviewResponse aiResponse = aiInterviewClient.getNextQuestion(aiRequest);
                String nextQuestion = aiResponse.getRealtime().getNextQuestion();
                String aiFeedback = null;
                Integer aiScore = 0;

                // 리포트 처리 (마지막 턴 or AI가 pass_fail 판단 시)
                if (aiResponse.getRealtime().getReport() != null) {
                        Map<String, Object> report = aiResponse.getRealtime().getReport();
                        aiFeedback = (String) report.get("feedback_comment");
                        if (report.get("competency_scores") instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Double> scores = (Map<String, Double>) report.get("competency_scores");
                                if (scores != null && !scores.isEmpty()) {
                                        double avg = scores.values().stream().mapToDouble(Double::doubleValue).average()
                                                        .orElse(0.0);
                                        aiScore = (int) (avg * 20); // 5.0 만점 -> 100점 만점
                                }
                        }
                }

                // 5. 면접 종료 조건 확인
                if (interview.getCurrentTurn() >= interview.getTotalTurns()) {
                        if (aiScore == 0)
                                aiScore = 80; // Fallback
                        if (aiFeedback == null)
                                aiFeedback = "면접이 종료되었습니다. 수고하셨습니다.";

                        interview.completeInterview(aiScore, aiFeedback);
                        interviewRepository.save(interview);

                        return InterviewQuestionResponse.builder()
                                        .interviewId(interview.getInterviewId())
                                        .currentTurn(interview.getCurrentTurn())
                                        .isCompleted(true)
                                        .finalScore(aiScore)
                                        .finalFeedback(aiFeedback)
                                        .build();
                }

                // 6. 다음 질문 저장
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
                                .reactionType(aiResponse.getRealtime().getReaction() != null
                                                ? aiResponse.getRealtime().getReaction().getType()
                                                : null)
                                .reactionText(aiResponse.getRealtime().getReaction() != null
                                                ? aiResponse.getRealtime().getReaction().getText()
                                                : null)
                                .aiSystemReport(aiResponse.getRealtime().getReport())
                                .requestedEvidence(aiResponse.getRealtime().getRequestedEvidence())
                                .probeGoal(aiResponse.getRealtime().getProbeGoal())
                                .build();
        }

        /**
         * 답변 수정 (Dialogic Feedback Loop)
         */
        @Transactional
        public InterviewQuestionResponse modifyAnswer(Long userId, InterviewMessageRequest request) {
                log.info("답변 수정 요청: userId={}, interviewId={}", userId, request.getInterviewId());

                Interview interview = interviewRepository.findByInterviewIdAndUserId(request.getInterviewId(), userId)
                                .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다"));

                if (interview.getStatus() != Status.IN_PROGRESS) {
                        throw new IllegalStateException("진행 중인 면접이 아닙니다");
                }

                int currentTurn = interview.getCurrentTurn();
                int targetTurn = currentTurn - 1;

                if (targetTurn < 1) {
                        throw new IllegalStateException("수정할 답변이 없습니다. (첫 질문 단계)");
                }

                Resume resume = resumeRepository.findById(interview.getResumeId())
                                .orElseThrow(() -> new IllegalStateException("이력서를 찾을 수 없습니다."));

                // 기존 답변(Candidate) 업데이트
                InterviewMessage candidateMsg = interviewMessageRepository.findByInterviewIdAndTurnNumberAndRole(
                                interview.getInterviewId(), targetTurn, Role.CANDIDATE)
                                .orElseThrow(() -> new IllegalArgumentException("이전 답변을 찾을 수 없습니다."));

                candidateMsg.updateMessage(request.getAnswer());

                // AI 재요청
                Map<String, Object> resumeContent = buildResumeContent(resume);
                List<String> portfolioFiles = portfolioRepository
                                .findByResumeIdOrderByDisplayOrder(resume.getResumeId())
                                .stream()
                                .map(Portfolio::getFilePath)
                                .collect(Collectors.toList());

                // 직무 정규화
                String normalizedJobCategory = org.zerock.nextenter.common.constants.JobConstants
                                .normalize(interview.getJobCategory());

                AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                                .id(userId.toString())
                                .targetRole(normalizedJobCategory) // Normalized
                                .resumeContent(resumeContent)
                                .lastAnswer(request.getAnswer())
                                .portfolioFiles(portfolioFiles)
                                .systemInstruction(
                                                "This is a revised answer from the candidate. Please re-evaluate and provide feedback.")
                                .build();

                AiInterviewResponse aiResponse = aiInterviewClient.getNextQuestion(aiRequest);
                String nextQuestion = aiResponse.getRealtime().getNextQuestion();

                // 질문(Interviewer) 업데이트
                InterviewMessage interviewerMsg = interviewMessageRepository.findByInterviewIdAndTurnNumberAndRole(
                                interview.getInterviewId(), currentTurn, Role.INTERVIEWER)
                                .orElseThrow(() -> new IllegalArgumentException("다음 질문 메시지를 찾을 수 없습니다."));

                interviewerMsg.updateMessage(nextQuestion);

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

        // ===== Private Methods =====

        private Map<String, Object> buildResumeContent(Resume resume) {
                Map<String, Object> content = new HashMap<>();

                content.put("title", resume.getTitle());

                // 직무 정규화
                String normalizedJobCategory = org.zerock.nextenter.common.constants.JobConstants
                                .normalize(resume.getJobCategory());
                content.put("job_category", normalizedJobCategory);

                content.put("education", parseJsonSafe(resume.getEducations()));
                content.put("professional_experience", parseJsonSafe(resume.getCareers()));
                content.put("project_experience", parseJsonSafe(resume.getExperiences()));
                content.put("skills", parseJsonSafe(resume.getSkills()));

                if (resume.getExtractedText() != null) {
                        content.put("raw_text", resume.getExtractedText());
                }

                return content;
        }

        private Object parseJsonSafe(String json) {
                if (json == null || json.isBlank()) {
                        return Collections.emptyList();
                }
                try {
                        return objectMapper.readValue(json, new TypeReference<Object>() {
                        });
                } catch (Exception e) {
                        log.warn("Failed to parse resume JSON field: {}", e.getMessage());
                        return Collections.emptyList();
                }
        }
}
