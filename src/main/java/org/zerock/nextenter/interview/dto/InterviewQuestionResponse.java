package org.zerock.nextenter.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionResponse {

    private Long interviewId;
    private Integer currentTurn;
    private String question;
    private Boolean isCompleted;
    private Integer finalScore;
    private String finalFeedback;

    // --- AI Rich Metadata ---
    private String reactionType;
    private String reactionText;
    @Builder.Default
    private java.util.Map<String, Object> aiSystemReport = new java.util.HashMap<>(); // Competency scores, etc.
    
    @Builder.Default
    private java.util.Map<String, Object> aiEvaluation = new java.util.HashMap<>();
    
    @Builder.Default
    private java.util.List<String> requestedEvidence = new java.util.ArrayList<>();
    private String probeGoal;
}