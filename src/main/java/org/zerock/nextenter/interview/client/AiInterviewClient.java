package org.zerock.nextenter.interview.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AiInterviewClient {

    private final RestClient restClient;

    // PII Regex Patterns (Interview Xpert & Security best practices)
    // 주민등록번호, 전화번호, 이메일, 주소 등을 마스킹
    private static final String PHONE_REGEX = "01[016789]-?\\d{3,4}-?\\d{4}";
    private static final String EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}";
    private static final String RRN_REGEX = "\\d{6}-[1-4]\\d{6}"; // Simple RRN check

    public AiInterviewClient(@Value("${ai.server.url}") String aiServerUrl) {
        log.info("AI Server URL: {}", aiServerUrl);
        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .build();
    }

    public AiInterviewResponse getNextQuestion(AiInterviewRequest request) {
        // 1. PII Filtering (Security)
        String maskedAnswer = maskPII(request.getLastAnswer());
        request.setLastAnswer(maskedAnswer);

        // 2. STAR Technique Enforcement (Conversate)
        // 시스템에 STAR 구조를 강제하도록 힌트를 전달 (백엔드 지원 필요하지만, 우선 요청에 포함)
        request.setSystemInstruction(
                "Please provide feedback based on the STAR (Situation, Task, Action, Result) technique. If the answer lacks specific actions or results, ask follow-up questions to clarify.");

        log.info("Requesting next question to AI: role={}, lastAnswer={}",
                request.getTargetRole(),
                request.getLastAnswer() != null ? "Present (Masked)" : "Null (Start)");

        try {
            return restClient.post()
                    .uri("/interview/next")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiInterviewResponse.class);
        } catch (Exception e) {
            log.error("Error communicating with AI Audit Server", e);
            throw new RuntimeException("AI Server connection failed: " + e.getMessage());
        }
    }

    /**
     * 개인정보 마스킹 (PII Filter)
     */
    private String maskPII(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String masked = input;
        masked = masked.replaceAll(PHONE_REGEX, "[PHONE_REDACTED]");
        masked = masked.replaceAll(EMAIL_REGEX, "[EMAIL_REDACTED]");
        masked = masked.replaceAll(RRN_REGEX, "[RRN_REDACTED]");
        return masked;
    }

    // --- DTOs for AI Communication ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiInterviewRequest {
        @Builder.Default
        private String id = "USER_TEMP";

        @JsonProperty("target_role")
        private String targetRole;

        @JsonProperty("resume_content")
        private Map<String, Object> resumeContent;

        @JsonProperty("last_answer")
        private String lastAnswer;

        @JsonProperty("system_instruction") // New field for prompt injection
        private String systemInstruction;

        // Optional fields
        private Map<String, Object> classification;
        private Map<String, Object> evaluation;
        private Map<String, Object> portfolio;

        @JsonProperty("portfolio_files")
        private List<String> portfolioFiles; // List of file paths/URLs
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiInterviewResponse {
        private String status;

        @JsonProperty("resume_id")
        private String resumeId;

        @JsonProperty("target_role")
        private String targetRole;

        private AiRealtimeResponse realtime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiRealtimeResponse {
        @JsonProperty("next_question")
        private String nextQuestion;

        private AiReaction reaction;

        @JsonProperty("probe_goal")
        private String probeGoal;

        @JsonProperty("requested_evidence")
        private List<String> requestedEvidence;

        private Map<String, Object> report;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiReaction {
        private String type;
        private String text;
    }
}
