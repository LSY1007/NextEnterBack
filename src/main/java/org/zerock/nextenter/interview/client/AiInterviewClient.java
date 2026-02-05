package org.zerock.nextenter.interview.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AiInterviewClient {

    private final ObjectMapper objectMapper;
    private final String aiServerUrl;

    // PII Regex Patterns (Interview Xpert & Security best practices)
    private static final String PHONE_REGEX = "01[016789]-?\\d{3,4}-?\\d{4}";
    private static final String EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}";
    private static final String RRN_REGEX = "\\d{6}-[1-4]\\d{6}";

    public AiInterviewClient(@Value("${ai.server.url}") String aiServerUrl, ObjectMapper objectMapper) {
        this.aiServerUrl = aiServerUrl;
        this.objectMapper = objectMapper;
        log.info("AI Server URL: {}", aiServerUrl);
    }

    public AiInterviewResponse getNextQuestion(AiInterviewRequest request) {
        // 1. PII Filtering (Security)
        String maskedAnswer = maskPII(request.getLastAnswer());
        request.setLastAnswer(maskedAnswer);

        // 2. STAR Technique Enforcement (Conversate)
        if (request.getSystemInstruction() == null) {
            request.setSystemInstruction(
                    "Please provide feedback based on the STAR (Situation, Task, Action, Result) technique. " +
                    "If the answer lacks specific actions or results, ask follow-up questions to clarify."
            );
        }

        log.info("Requesting next question to AI: role={}, lastAnswer={}",
                request.getTargetRole(),
                request.getLastAnswer() != null ? "Present (Masked)" : "Null (Start)");

        try {
            // 3. Serializing JSON with ObjectMapper (explicit)
            String jsonBody = objectMapper.writeValueAsString(request);
            log.debug("Sending JSON Body: {}", jsonBody);

            // 4. Using pure RestTemplate with explicit UTF-8 encoding
            // This resolved the 400 Bad Request / Encoding issues in ResumeAiService
            RestTemplate directRestTemplate = new RestTemplate();
            directRestTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            String url = aiServerUrl + "/interview/next";
            log.info("🚀 [AI-INTERVIEW] POST Request to: {}", url);

            ResponseEntity<String> responseEntity = directRestTemplate.postForEntity(url, requestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return objectMapper.readValue(responseEntity.getBody(), AiInterviewResponse.class);
            } else {
                throw new RuntimeException("AI Server responded with status: " + responseEntity.getStatusCode());
            }

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

    /**
     * 면접 종료 및 최종 점수 요청
     */
    /**
     * 면접 종료 및 최종 점수 요청
     */
    public AiFinalizeResponse finalizeInterview(String sessionId, List<Map<String, Object>> chatHistory) {
        log.info("🏁 [AI-FINALIZE] Requesting final score for session: {}, History size: {}", sessionId, chatHistory != null ? chatHistory.size() : 0);

        try {
            // 1. Prepare Request Body
            Map<String, Object> requestBody = Map.of(
                "id", sessionId,
                "chat_history", chatHistory != null ? chatHistory : List.of()
            );
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 2. Configure Headers
            RestTemplate directRestTemplate = new RestTemplate();
            directRestTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            // 3. Send Request
            String url = aiServerUrl + "/interview/finalize";
            log.info("🚀 [AI-FINALIZE] POST Request to: {}", url);

            ResponseEntity<String> responseEntity = directRestTemplate.postForEntity(url, requestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                AiFinalizeResponse response = objectMapper.readValue(responseEntity.getBody(), AiFinalizeResponse.class);
                log.info("✅ [AI-FINALIZE] Score: {}, Result: {}", response.getTotalScore(), response.getResult());
                return response;
            } else {
                throw new RuntimeException("AI Finalize responded with status: " + responseEntity.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ [AI-FINALIZE] Error: {}", e.getMessage());
            // Return fallback response instead of throwing
            AiFinalizeResponse fallback = new AiFinalizeResponse();
            fallback.setTotalScore(0.0);
            fallback.setResult("Error");
            fallback.setError(e.getMessage());
            return fallback;
        }
    }

    // --- DTOs for AI Communication ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiFinalizeResponse {
        @JsonProperty("total_score")
        private Double totalScore;
        
        private String result;
        private Map<String, Object> stats;
        private String error;
    }

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

        @JsonProperty("system_instruction") 
        private String systemInstruction;

        // Optional fields
        private Map<String, Object> classification;
        private Map<String, Object> evaluation;
        private Map<String, Object> portfolio;

        @JsonProperty("portfolio_files")
        @Builder.Default
        private List<String> portfolioFiles = new java.util.ArrayList<>();

        @JsonProperty("total_turns")
        private Integer totalTurns; // ✅ 횟수 정보 추가

        // [NEW] Stateless Context Support
        @JsonProperty("chat_history")
        private List<Map<String, Object>> chatHistory;

        @JsonProperty("difficulty")
        private String difficulty;
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

        @JsonProperty("analysis_result")
        private Map<String, Object> analysisResult; // ✅ [NEW] Analysis Data from Python
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiReaction {
        private String type;
        private String text;
    }
}
