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
    private java.util.Map<String, Object> aiSystemReport; // Competency scores, etc.
    private java.util.Map<String, Object> aiEvaluation;
    private java.util.List<String> requestedEvidence;
    private String probeGoal;
}