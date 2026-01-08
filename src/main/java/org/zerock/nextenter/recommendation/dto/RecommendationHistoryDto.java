package org.zerock.nextenter.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationHistoryDto {
    private Long recommendationId;
    private Long resumeId;
    private String resumeTitle;
    private Integer jobCount;        // 추천받은 공고 수 (5개)
    private String topJobTitle;      // 1순위 공고 제목
    private String topCompanyName;   // 1순위 회사명
    private Integer topScore;        // 1순위 점수
    private Integer creditUsed;
    private LocalDateTime createdAt;
}