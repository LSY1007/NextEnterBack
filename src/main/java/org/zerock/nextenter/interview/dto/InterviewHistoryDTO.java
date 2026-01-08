package org.zerock.nextenter.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewHistoryDTO {

    private Long interviewId;
    private String jobCategory;
    private String difficulty;
    private Integer totalTurns;
    private Integer currentTurn;
    private String status;
    private Integer finalScore;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}