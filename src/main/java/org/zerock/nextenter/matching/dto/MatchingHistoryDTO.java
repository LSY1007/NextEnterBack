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
    private Long jobId;
    private String grade;
    private String matchingType;
    private LocalDateTime createdAt;
}