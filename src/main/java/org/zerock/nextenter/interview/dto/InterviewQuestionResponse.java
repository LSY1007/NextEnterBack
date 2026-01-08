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
}