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

        // 1-2. 유저 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 2. 이력서 조회
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

        // 직무 정규화
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
                .jobCategory(normalizedJobCategory)
                .difficulty(difficulty)
                .totalTurns(requestTotalTurns)
                .currentTurn(0)
                .status(Status.IN_PROGRESS)
                .build();

        interviewRepository.save(interview);
        log.info("면접 세션 생성 완료: interviewId={}, userId={}, jobCategory={}",
                interview.getInterviewId(), userId, normalizedJobCategory);

        // 5. Context 구성
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
        log.info("========================================");

        // 6. AI에게 첫 질문 요청
        AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                .id(userId.toString())
                .targetRole(normalizedJobCategory)
                .resumeContent(resumeContent)
                .lastAnswer(null) // 첫 질문이므로 null
                .portfolioFiles(portfolioFiles)
                .totalTurns(interview.getTotalTurns())
                .difficulty(interview.getDifficulty().name())
                .chatHistory(Collections.emptyList())
                .build();

        AiInterviewResponse aiResponse;
        try {
            aiResponse = aiInterviewClient.getNextQuestion(aiRequest);
            log.info("✅ [AI-RESPONSE] AI Server 응답 성공: {}", aiResponse.getRealtime().getNextQuestion());
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

        InterviewMessage answerMessage = InterviewMessage.builder()
                .interviewId(interview.getInterviewId())
                .turnNumber(interview.getCurrentTurn())
                .role(Role.CANDIDATE)
                .message(request.getAnswer())
                .build();
        interviewMessageRepository.save(answerMessage);
        
        // [DEBUG] Log saved answer
        System.out.println("💾 [Service] Saved Answer Message. Turn: " + interview.getCurrentTurn() + ", Content: " + request.getAnswer());

        // 4. AI에게 답변 전송
        Map<String, Object> resumeContent = buildResumeContent(resume);

        List<String> portfolioFiles = portfolioRepository
                .findByResumeIdOrderByDisplayOrder(resume.getResumeId())
                .stream()
                .map(Portfolio::getFilePath)
                .collect(Collectors.toList());

        // [NEW] History 구성
        List<Map<String, Object>> fullHistory = buildChatHistory(interview.getInterviewId());
        
        // Remove the duplicated recent user answer if present (since it's sent as lastAnswer)
        List<Map<String, Object>> chatHistory = fullHistory.stream().collect(Collectors.toList());
        if (!chatHistory.isEmpty()) {
             Map<String, Object> last = chatHistory.get(chatHistory.size() - 1);
             // [DEBUG] Log last history item
             System.out.println("🔍 [Service] Last history item role: " + last.get("role") + ", Content: " + last.get("content"));
             
             if ("user".equals(last.get("role"))) {
                 chatHistory.remove(chatHistory.size() - 1);
                 System.out.println("✂️ [Service] Removed last user message from history to avoid duplication with 'lastAnswer'");
             }
        }

        // Classification Objects
        Map<String, Object> classification = new HashMap<>();
        String resumeRole = org.zerock.nextenter.common.constants.JobConstants
                .normalize(resume.getJobCategory());
        classification.put("predicted_role", resumeRole);
        
        Map<String, Object> evaluation = new HashMap<>();

        String interviewRole = org.zerock.nextenter.common.constants.JobConstants
                .normalize(interview.getJobCategory());

        AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                .id(userId.toString())
                .targetRole(interviewRole)
                .resumeContent(resumeContent)
                .lastAnswer(request.getAnswer())
                .portfolioFiles(portfolioFiles)
                .totalTurns(interview.getTotalTurns())
                .difficulty(interview.getDifficulty().name())
                .chatHistory(chatHistory)
                .classification(classification)
                .evaluation(evaluation)
                .build();
                
        aiRequest.setChatHistory(chatHistory);

        AiInterviewResponse aiResponse = aiInterviewClient.getNextQuestion(aiRequest);
        String nextQuestion = aiResponse.getRealtime().getNextQuestion();
        // 리포트 처리
        if (aiResponse.getRealtime().getReport() != null) {
            // Report is available
        }

        // 5. AI 질문 저장
        interview.incrementTurn();
        InterviewMessage questionMessage = InterviewMessage.builder()
                .interviewId(interview.getInterviewId())
                .turnNumber(interview.getCurrentTurn())
                .role(Role.INTERVIEWER)
                .message(nextQuestion)
                .build();
        interviewMessageRepository.save(questionMessage);

        // 6. AI 분석 결과 저장 (System Phase)
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

        // 7. 종료 조건 확인
        boolean isCompleted = false;
        if (interview.getCurrentTurn() >= interview.getTotalTurns()) {
            isCompleted = true;
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
     * 답변 수정
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

        String normalizedJobCategory = org.zerock.nextenter.common.constants.JobConstants
                .normalize(interview.getJobCategory());

        List<Map<String, Object>> chatHistory = buildChatHistory(interview.getInterviewId()).stream()
            .collect(Collectors.toList());
            
        // Modify history: remove last user answer to avoid duplication
        if (!chatHistory.isEmpty() && chatHistory.get(chatHistory.size()-1).get("role").equals("user")) {
            chatHistory.remove(chatHistory.size() - 1);
        }

        AiInterviewRequest aiRequest = AiInterviewRequest.builder()
                .id(userId.toString())
                .targetRole(normalizedJobCategory)
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
        Map<String, Object> content = new HashMap<>();
        content.put("title", resume.getTitle());
        
        String normalizedJobCategory = org.zerock.nextenter.common.constants.JobConstants
                .normalize(resume.getJobCategory());
        content.put("job_category", normalizedJobCategory);
        content.put("raw_job_category", resume.getJobCategory());

        int totalYears = calculateTotalExperience(resume.getCareers());
        content.put("total_experience_years", totalYears);

        content.put("education", parseJsonSafe(resume.getEducations()));
        content.put("professional_experience", parseJsonSafe(resume.getCareers()));
        content.put("project_experience", parseJsonSafe(resume.getExperiences()));

        List<String> skills = parseSkills(resume.getSkills());
        content.put("skills", skills);

        if (resume.getExtractedText() != null && !resume.getExtractedText().isBlank()) {
            content.put("raw_text", resume.getExtractedText());
        } else {
             if (content.get("raw_text") == null) {
                  content.put("raw_text", "Skills: " + skills + "\nTitle: " + resume.getTitle());
             }
        }
        return content;
    }

    private Object parseJsonSafe(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Object>() {});
        } catch (Exception e) {
            log.warn("Failed to parse resume JSON field: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> parseSkills(String skills) {
        if (skills == null || skills.isBlank()) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildChatHistory(Long interviewId) {
        List<InterviewMessage> messages = interviewMessageRepository.findByInterviewIdOrderByTurnNumberAsc(interviewId);
        return messages.stream()
            .map(msg -> {
                Map<String, Object> item = new HashMap<>();
                if (msg.getRole() == Role.SYSTEM) {
                    item.put("role", "system");
                    item.put("type", "analysis");
                    try {
                        item.put("content", objectMapper.readValue(msg.getMessage(), new TypeReference<Map<String, Object>>() {}));
                    } catch (Exception e) {
                        item.put("content", msg.getMessage());
                    }
                } else {
                    String role = (msg.getRole() == Role.INTERVIEWER) ? "assistant" : "user";
                    item.put("content", msg.getMessage());
                    item.put("role", role);
                    item.put("type", (msg.getRole() == Role.INTERVIEWER) ? "question" : "answer");
                }
                return item;
            })
            .collect(Collectors.toList());
    }

    private int calculateTotalExperience(String careersJson) {
        if (careersJson == null || careersJson.isBlank()) {
            return 0;
        }
        try {
            List<Map<String, Object>> careers = objectMapper.readValue(careersJson, new TypeReference<List<Map<String, Object>>>() {});
            if (careers == null || careers.isEmpty()) {
                return 0;
            }
            
            int totalMonths = 0;
            for (Map<String, Object> career : careers) {
                if (career.containsKey("period")) {
                     Object periodObj = career.get("period");
                     if (periodObj != null) {
                         totalMonths += parsePeriodToMonths(periodObj.toString());
                     }
                }
            }
            return totalMonths / 12;
        } catch (Exception e) {
            log.warn("경력 기간 계산 실패: {}", e.getMessage());
            return 0;
        }
    }

    private int parsePeriodToMonths(String period) {
        try {
            String[] parts = period.split("~");
            if (parts.length != 2) return 0;
            
            String start = parts[0].trim().replace(" ", "");
            String end = parts[1].trim().replace(" ", "");
            
            if (end.equals("재직중") || end.equalsIgnoreCase("Present")) {
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
            // ignore
        }
        return 0;
    }
}
