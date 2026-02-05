package org.zerock.nextenter.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionResponse {

    private Long interviewId;
    private Integer currentTurn;
    private String question;

    // 프론트엔드 호환성: isFinished 필드 추가 (isCompleted 대신)
    private Boolean isFinished;

    @Deprecated // isFinished로 대체, 하위 호환성 유지
    private Boolean isCompleted;

    private Integer finalScore;
    private String finalFeedback;

    // 면접 완료 시 최종 결과 (프론트엔드 InterviewResponse.finalResult와 매칭)
    private FinalResult finalResult;

    // --- AI Rich Metadata ---
    private String reactionType;
    private String reactionText;
    @Builder.Default
    private Map<String, Object> aiSystemReport = new java.util.HashMap<>();

    @Builder.Default
    private Map<String, Object> aiEvaluation = new java.util.HashMap<>();

    @Builder.Default
    private List<String> requestedEvidence = new java.util.ArrayList<>();
    private String probeGoal;

    /**
     * 면접 완료 시 최종 결과 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinalResult {
        private Integer finalScore;
        private String result;  // "Pass" or "Fail"
        private String finalFeedback;
        private Map<String, Number> competencyScores;
        private List<String> strengths;
        private List<String> gaps;
    }
}