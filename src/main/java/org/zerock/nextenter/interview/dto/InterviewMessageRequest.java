package org.zerock.nextenter.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewMessageRequest {

    @NotNull(message = "면접 ID는 필수입니다")
    private Long interviewId;

    @NotBlank(message = "답변 내용은 필수입니다")
    private String answer;
}