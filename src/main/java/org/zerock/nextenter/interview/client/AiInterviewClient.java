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

    public AiInterviewClient(@Value("${ai.server.url}") String aiServerUrl) {
        log.info("AI Server URL: {}", aiServerUrl);
        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .build();
    }

    public AiInterviewResponse getNextQuestion(AiInterviewRequest request) {
        log.info("Requesting next question to AI: role={}, lastAnswer={}",
                request.getTargetRole(),
                request.getLastAnswer() != null ? "Present" : "Null (Start)");

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

        // Optional fields
        private Map<String, Object> classification;
        private Map<String, Object> evaluation;
        private Map<String, Object> portfolio;
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
