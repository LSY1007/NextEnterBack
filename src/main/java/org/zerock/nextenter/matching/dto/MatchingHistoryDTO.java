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
public class MatchingHistoryDTO {

    private Long matchingId;
    private Long resumeId;
    private Long userId;
    private Long jobId;
    private String jobStatus;
    private String companyName;
    private Double score;
    private String grade;
    private String resumeGrade;
    private String experienceLevel;
    private String missingSkills;
    private String feedback;
    private String pros;
    private String cons;
    private String matchingType;
    private LocalDateTime createdAt;
}
