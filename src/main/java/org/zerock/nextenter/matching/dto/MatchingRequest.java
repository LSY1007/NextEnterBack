package org.zerock.nextenter.matching.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchingRequest {

    @NotNull(message = "이력서 ID는 필수입니다")
    private Long resumeId;

    @NotNull(message = "공고 ID는 필수입니다")
    private Long jobId;

    @NotNull(message = "등급은 필수입니다")
    private String grade; // S, A, B, C, F

    private String missingSkills;

    private String cons;

    private String feedback;

    private String pros;

    private String matchingType; // MANUAL, AI_RECOMMEND
}