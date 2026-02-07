package org.zerock.nextenter.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResultDTO {

    private Long matchingId;
    private Long resumeId;
    private Long userId;
    private Long jobId;
    private String companyName;
    private Double score;
    private String grade;
    private String missingSkills;
    private String cons;
    private String feedback;
    private String pros;
    private String matchingType;
    private LocalDateTime createdAt;
}
