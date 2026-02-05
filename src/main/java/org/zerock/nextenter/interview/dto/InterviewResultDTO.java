package org.zerock.nextenter.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResultDTO {

    private Long interviewId;
    private Long userId;
    private Long resumeId;
    private String jobCategory;
    private String difficulty;
    private Integer totalTurns;
    private Integer currentTurn;
    private String status;
    private Integer finalScore;
    private String finalFeedback;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // AI 상세 분석 결과
    private java.util.Map<String, Double> competencyScores;
    private java.util.List<String> strengths;
    private java.util.List<String> gaps;

    // 면접 질문/답변 내역
    private List<MessageDto> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDto {
        private Long messageId;
        private Integer turnNumber;
        private String role;
        private String message;
        private LocalDateTime createdAt;
    }
}
