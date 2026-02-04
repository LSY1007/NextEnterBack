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
import org.zerock.nextenter.user.repository.UserRepository;

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
        private final UserRepository userRepository;
        private final AiInterviewClient aiInterviewClient;
        private final ObjectMapper objectMapper;

        /**
         * 면접 시작
         */
        @Transactional
        public InterviewQuestionResponse startInterview(Long userId, InterviewStartRequest request) {
                // 1. 진행 중인 면접이 있다면 자동 취소 (Auto-Cancel)
                List<Interview> existingInterviews = interviewRepository.findByUserIdAndStatus(userId, Status.IN_PROGRESS);
                if (!existingInterviews.isEmpty()) {
                        log.info("Found {} active interviews for user {}. Auto-cancelling them.", existingInterviews.size(), userId);
                        for (Interview existing : existingInterviews) {
                                existing.cancelInterview();
                        }
                        interviewRepository.saveAll(existingInterviews);
                }

                // 1-2. 유저 존재 확인 (추가)
                userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

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

                int requestTotalTurns = request.getTotalTurns() != null ? request.getTotalTurns() : 5;
                if (requestTotalTurns < 3) {
                    log.warn("Requested totalTurns {} is too small. Forcing minimum 3.", requestTotalTurns);
                    requestTotalTurns = 3;
                }

                // 4. 면접 세션 생성
                Interview interview = Interview.builder()
                                .userId(userId)
                                .resumeId(request.getResumeId())
                                .jobCategory(normalizedJobCategory) // Normalized
                                .difficulty(difficulty)
<<<<<<< Updated upstream
                                .totalTurns(requestTotalTurns)
                                .totalTurns(request.getTotalTurns() != null ? request.getTotalTurns() : 7) // [FIX] 기본값 7 (6질문 + 1종료)
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

                log.info("========================================");
                log.info("🤖 [AI-REQUEST] AI 엔진 요청 준비");
                log.info("🤖 [AI-REQUEST] userId: {}", userId);
                log.info("🤖 [AI-REQUEST] targetRole: {}", normalizedJobCategory);
                log.info("🤖 [AI-REQUEST] portfolioFiles 개수: {}", portfolioFiles.size());
                if (!portfolioFiles.isEmpty()) {
                        log.info("🤖 [AI-REQUEST] portfolioFiles: {}", portfolioFiles);
                }
                log.info("🤖 [AI-REQUEST] resumeContent 키: {}", resumeContent.keySet());
                log.info("========================================");

                // 6. AI에게 첫 질문 요청
                AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                                .id(userId.toString())
                                .targetRole(normalizedJobCategory) // Normalized
                                .resumeContent(resumeContent)
                                .resumeContent(resumeContent)
                                .lastAnswer(null) // 첫 질문이므로 null
                                .portfolioFiles(portfolioFiles)
                                .totalTurns(interview.getTotalTurns()) // ✅ 횟수 정보 추가
                                .difficulty(interview.getDifficulty().name()) // ✅ 난이도 전달
                                .chatHistory(Collections.emptyList()) // ✅ 첫 시작이므로 빈 History
                                .build();

                log.info("AI Server 요청 준비: targetRole={}, resumeId={}", aiRequest.getTargetRole(),
                                resume.getResumeId());

                AiInterviewResponse aiResponse;
                try {
                        aiResponse = aiInterviewClient.getNextQuestion(aiRequest);
                        log.info("✅ [AI-RESPONSE] AI Server 응답 성공");
                        log.info("✅ [AI-RESPONSE] 첫 질문: {}", aiResponse.getRealtime().getNextQuestion());
                } catch (Exception e) {
                        log.error("❌ [AI-RESPONSE] AI Server 연동 실패", e);
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
                
                // [DEBUG] Check Project Experience raw data
                log.info("🔍 [DEBUG] Raw Project Experience JSON: {}", resume.getExperiences());

                List<String> portfolioFiles = portfolioRepository
                                .findByResumeIdOrderByDisplayOrder(resume.getResumeId())
                                .stream()
                                .map(Portfolio::getFilePath)
                                .collect(Collectors.toList());

                // [NEW] History 구성 (현재 답변 제외, 이전 내역만)
                // 왜냐하면 last_answer로 현재 답변을 별도로 보내기 때문
                List<Map<String, Object>> fullHistory = buildChatHistory(interview.getInterviewId());
                log.info("🔍 [DEBUG] Built Chat History for Interview ID {}: {} items", interview.getInterviewId(), fullHistory.size());

                List<Map<String, Object>> chatHistory = fullHistory.stream().collect(Collectors.toList());
                if (!chatHistory.isEmpty()) {
                        Map<String, Object> last = chatHistory.get(chatHistory.size() - 1);
                        // If the last item is the USER's current answer, remove it.
                        // (Since we are sending it via lastAnswer field)
                        if ("user".equals(last.get("role"))) {
                                chatHistory.remove(chatHistory.size() - 1);
                                log.info("🔍 [DEBUG] Removed duplicate user answer from history.");
                        }
                }

                // [NEW] Classification & Evaluation Objects
                Map<String, Object> classification = new HashMap<>();
                // classification(분류): 지원자 이력서 기반 직무 (Resume 기반)
                String resumeRole = org.zerock.nextenter.common.constants.JobConstants
                                .normalize(resume.getJobCategory());
                classification.put("predicted_role", resumeRole);
                
                Map<String, Object> evaluation = new HashMap<>();
                // evaluation(평가): 초기값이므로 비워두거나 기본값 설정
                
                // target_role(목표): 기업 채용 기준/면접 대상 직무 (Interview 기반)
                String interviewRole = org.zerock.nextenter.common.constants.JobConstants
                                .normalize(interview.getJobCategory());

                AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                                .id(userId.toString())
                                .targetRole(interviewRole) // Normalized Target Role
                                .resumeContent(resumeContent)
                                .lastAnswer(request.getAnswer())
                                .portfolioFiles(portfolioFiles)
                                .totalTurns(interview.getTotalTurns()) // ✅ 횟수 정보 추가
                                .difficulty(interview.getDifficulty().name()) // ✅ 난이도 전달
                                .chatHistory(chatHistory) // ✅ 대화 내역 전달 (Lombok builder uses field name)
                                .classification(classification) // ✅ 분류 정보 추가 (이력서 기반)
                                .evaluation(evaluation) // ✅ 평가 정보 추가 (null 방지)
                                .build();

                // Note: AiInterviewRequest 필드명이 chat_history(서버) vs chatHistory(DTO) 확인 필요
                // AiInterviewClient.java 의 AiInterviewRequest 내용을 보면 @JsonProperty("chat_history") 가 붙어있으므로 chatHistory 필드를 쓰면 됨.
                // 하지만 builder() 사용 시 필드명을 맞춰야 함.
                aiRequest.setChatHistory(chatHistory); 

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

                // 5. AI 답변 저장 (Interviewer Role) - [FIX] 종료 체크를 저장 후로 이동
                interview.incrementTurn();
                InterviewMessage questionMessage = InterviewMessage.builder()
                                .interviewId(interview.getInterviewId())
                                .turnNumber(interview.getCurrentTurn())
                                .role(Role.INTERVIEWER)
                                .message(nextQuestion)
                                .build();
                interviewMessageRepository.save(questionMessage);

                // 6. AI 분석 결과 저장 (System Role - State Restoration)
                if (aiResponse.getRealtime() != null && aiResponse.getRealtime().getAnalysisResult() != null) {
                    try {
                        String analysisJson = objectMapper.writeValueAsString(aiResponse.getRealtime().getAnalysisResult());
                        InterviewMessage analysisMessage = InterviewMessage.builder()
                                .interviewId(interview.getInterviewId())
                                .turnNumber(interview.getCurrentTurn())
                                .role(Role.SYSTEM)
                                .message(analysisJson)
                                .build();
                        interviewMessageRepository.save(analysisMessage);
                        log.info("💾 [System] Analysis saved for turn {}", interview.getCurrentTurn());
                    } catch (Exception e) {
                        log.error("Failed to save analysis result", e);
                    }
                }

                // 7. 종료 조건 확인 (Turn 7 도달 시 Finalize)
                boolean isCompleted = false;
                if (interview.getCurrentTurn() >= interview.getTotalTurns()) {
                    isCompleted = true;
                    // 마지막 메시지("수고하셨습니다")가 저장된 상태에서 최종 점수 산출
                    AiInterviewClient.AiFinalizeResponse finalResult = aiInterviewClient.finalizeInterview(
                        interview.getInterviewId().toString(),
                        buildChatHistory(interview.getInterviewId())
                    );

                    if (finalResult.getTotalScore() != null) {
                         interview.completeInterview(
                            (int) Math.round(finalResult.getTotalScore() * 20),
                            finalResult.getResult()
                        );
                    } else {
                         interview.completeInterview(80, "Pass"); // Fallback
                    }
                    log.info("🏁 Interview Completed: ID={}, Score={}", interview.getInterviewId(), interview.getFinalScore());
                }

                interviewRepository.save(interview);

                // 8. 응답 반환
                return InterviewQuestionResponse.builder()
                                .isCompleted(isCompleted)
                                .question(nextQuestion)
                                .interviewId(interview.getInterviewId())
                                .currentTurn(interview.getCurrentTurn())
                                .aiSystemReport(aiResponse.getRealtime().getReport())
                                .requestedEvidence(aiResponse.getRealtime().getRequestedEvidence())
                                .probeGoal(aiResponse.getRealtime().getProbeGoal())
                                .reactionType(aiResponse.getRealtime().getReaction() != null ? aiResponse.getRealtime().getReaction().getType() : null)
                                .reactionText(aiResponse.getRealtime().getReaction() != null ? aiResponse.getRealtime().getReaction().getText() : null)
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

                // [NEW] History 구성 (수정 대상인 마지막 답변 이전까지의 History)
                // request.getAnswer()가 새로운 답변이 됨.
                // buildChatHistory는 DB에 있는 모든 메시지를 가져옴.
                // 하지만 수정 로직에서는 "기존 답변"을 대체하는 것이므로, 
                // DB에 저장된 "기존 답변"과 그 이후의 "다음 질문"은 제외해야 함.
                // 로직: targetTurn 이전까지의 메시지만 History로 간주.
                
                List<Map<String, Object>> chatHistory = buildChatHistory(interview.getInterviewId()).stream()
                    .filter(msg -> {
                        // turnNumber가 targetTurn보다 작은 것만 포함 (== 이전 턴들)
                        // 하지만 buildChatHistory에서 turnNumber를 안 가져오면 필터링 어려움.
                        // 일단 전체 다 가져와서 Python 엔진이 "last_answer"를 추가하면 
                        // 마지막 턴이 중복될 수 있음.
                        // 심플하게: 여기서는 전체 History 보내고 Python이 알아서 하길 기대하기보다
                        // 명시적으로 잘라주는게 좋음.
                        // 하지만 buildChatHistory 반환타입이 Map이라 turn 정보가 없음.
                        // buildChatHistory를 수정하거나 여기서 직접 구현.
                        return true; 
                    })
                    .collect(Collectors.toList());
                    
                // *Simple Fix*: DB 업데이트(331라인)가 이미 되었으므로,
                // chatHistory를 가져오면 "수정된 답변"이 포함되어 있음.
                // 그러므로 lastAnswer 필드는 비우거나, 중복을 감안해야 함.
                // or InterviewEngine이 "마지막 메시지가 User이면 덮어쓰기"? 
                // -> InterviewEngine 로직: if last_answer: append(last_answer)
                // 즉, history에 이미 있으면 중복됨.
                
                // 전략: modifyAnswer에서는 lastAnswer 필드를 보내고, 
                // chatHistory에서는 "해당 턴"을 제외해야 함.
                // 가장 쉬운 방법: History에서 마지막 아이템(수정된 답변) 제거.
                if (!chatHistory.isEmpty() && chatHistory.get(chatHistory.size()-1).get("role").equals("user")) {
                    chatHistory.remove(chatHistory.size() - 1);
                }

                AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                                .id(userId.toString())
                                .targetRole(normalizedJobCategory) // Normalized
                                .resumeContent(resumeContent)
                                .lastAnswer(request.getAnswer())
                                .portfolioFiles(portfolioFiles)
                                .difficulty(interview.getDifficulty().name())
                                .chatHistory(chatHistory)
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
                log.info("========================================");
                log.info("🔍 [AI-DATA] Resume 데이터 구성 시작");
                log.info("🔍 [AI-DATA] resumeId: {}", resume.getResumeId());

                Map<String, Object> content = new HashMap<>();

                content.put("title", resume.getTitle());
                log.info("🔍 [AI-DATA] title: {}", resume.getTitle());

                // 직무 정규화
                String normalizedJobCategory = org.zerock.nextenter.common.constants.JobConstants
                                .normalize(resume.getJobCategory());
                content.put("job_category", normalizedJobCategory);
                content.put("raw_job_category", resume.getJobCategory()); // 직무 연관성 분석용 원본
                log.info("🔍 [AI-DATA] job_category: {} (원본: {})", normalizedJobCategory, resume.getJobCategory());

                // 경력 기간 (총 년수) 계산 및 주입
                int totalYears = calculateTotalExperience(resume.getCareers());
                content.put("total_experience_years", totalYears);
                log.info("🔍 [AI-DATA] total_experience_years: {}년", totalYears);

                Object education = parseJsonSafe(resume.getEducations());
                content.put("education", education);
                log.info("🔍 [AI-DATA] education: {}", education);

                Object careers = parseJsonSafe(resume.getCareers());
                content.put("professional_experience", careers);
                log.info("🔍 [AI-DATA] professional_experience: {}", careers);

                Object experiences = parseJsonSafe(resume.getExperiences());
                content.put("project_experience", experiences);
                log.info("🔍 [AI-DATA] project_experience: {}", experiences);

                // ✅ Skills: CSV 파싱으로 변경 (기존 JSON 파싱 오류 해결)
                List<String> skills = parseSkills(resume.getSkills());
                content.put("skills", skills);
                log.info("🔍 [AI-DATA] skills (Parsed from CSV): {}", skills);

                if (resume.getExtractedText() != null && !resume.getExtractedText().isBlank()) {
                        String rawText = resume.getExtractedText();
                        content.put("raw_text", rawText);
                        log.info("🔍 [AI-DATA] raw_text 길이: {} chars", rawText.length());
                        int previewLength = Math.min(200, rawText.length());
                        log.info("🔍 [AI-DATA] raw_text 미리보기: {}", rawText.substring(0, previewLength));
                } else {
                        log.warn("⚠️ [AI-DATA] raw_text가 NULL이거나 비어있습니다! 파일 기반 이력서의 경우 면접 품질이 저하될 수 있습니다.");
                        // fallback: use skills/experience strings if available
                        if (content.get("raw_text") == null) {
                             content.put("raw_text", "Skills: " + skills + "\nTitle: " + resume.getTitle());
                        }
                }

                log.info("========================================");
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
                        log.warn("Failed to parse resume JSON field: {} (Value: {})", e.getMessage(), json);
                        return Collections.emptyList();
                }
        }

<<<<<<< Updated upstream
        private List<String> parseSkills(String skills) {
            if (skills == null || skills.isBlank()) {
                return Collections.emptyList();
            }
            // CSV parsing: Split by comma, trim, filter empty
            return java.util.Arrays.stream(skills.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())

            List<InterviewMessage> messages = interviewMessageRepository.findByInterviewIdOrderByTurnNumberAsc(interviewId);
        private List<Map<String, Object>> buildChatHistory(Long interviewId) {
            return messages.stream()
                .map(msg -> {
                    if (msg.getRole() == Role.SYSTEM) {
                    Map<String, Object> item = new HashMap<>();
                        item.put("role", "system");
                        item.put("type", "analysis");
                            // Parse JSON string back to Map/Object for Python
                        try {
                            item.put("content", objectMapper.readValue(msg.getMessage(), new TypeReference<Map<String, Object>>() {}));
                        } catch (Exception e) {
                            log.error("Failed to parse SYSTEM message content: {}", msg.getMessage());
                            item.put("content", msg.getMessage()); // Fallback
                        }
                    } else {
                        // Role Mapping: INTERVIEWER -> assistant, CANDIDATE -> user
                        String role = (msg.getRole() == Role.INTERVIEWER) ? "assistant" : "user";
                        item.put("content", msg.getMessage()); // Standard string content
                        item.put("role", role);
                        // Type: question vs answer
                        item.put("type", (msg.getRole() == Role.INTERVIEWER) ? "question" : "answer");
                    }
                    return item;
                .collect(Collectors.toList());
                })
        /**
         * 경력 기술서 JSON에서 총 경력 기간(개월 -> 년) 계산
         */
        private int calculateTotalExperience(String careersJson) {
                if (careersJson == null || careersJson.isBlank()) {
                        return 0;
                }
                try {
                        List<Map<String, Object>> careers = objectMapper.readValue(careersJson, new TypeReference<List<Map<String, Object>>>() {});
                        if (careers == null || careers.isEmpty()) {
                                return 0;
                        }
                        
                        for (Map<String, Object> career : careers) {
                        int totalMonths = 0;
                                        Object periodObj = career.get("period");
                                if (career.containsKey("period")) {
                                        if (periodObj != null) {
                                                totalMonths += parsePeriodToMonths(periodObj.toString());
                                        }
                                }
                        }
                } catch (Exception e) {
                        return totalMonths / 12;
                        log.warn("경력 기간 계산 실패: {}", e.getMessage());
                        return 0;
                }
        }

        private int parsePeriodToMonths(String period) {
                try {
                        // 예상 포맷: "2020.03 ~ 2022.05"
                        String[] parts = period.split("~");
                        if (parts.length != 2)
                                return 0;
                        
                        String end = parts[1].trim().replace(" ", "");
                        String start = parts[0].trim().replace(" ", "");
                        
                        if (end.equals("재직중") || end.equalsIgnoreCase("Present")) {
                        // "재직중" 처리
                                java.time.LocalDate now = java.time.LocalDate.now();
                                end = now.getYear() + "." + String.format("%02d", now.getMonthValue());
                        }

                        String[] startParts = start.split("\\.");
                        String[] endParts = end.split("\\.");
                        
                        if (startParts.length >= 2 && endParts.length >= 2) {
                                int startYear = Integer.parseInt(startParts[0]);
                                int startMonth = Integer.parseInt(startParts[1]);
                                int endYear = Integer.parseInt(endParts[0]);
                                int endMonth = Integer.parseInt(endParts[1]);
                                
                                return Math.max(0, (endYear - startYear) * 12 + (endMonth - startMonth));
                        }
                } catch (Exception e) {
                        // ignore parsing errors
                }
                return 0;
        }
}
