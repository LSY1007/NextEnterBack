package org.zerock.nextenter.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationJobDto {
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Integer score;           // 매칭 점수 (0-100)
    private String grade;            // S, A, B, C, F
    private List<String> matchReasons;      // 매칭 이유
    private List<String> missingSkills;     // 부족한 스킬

    // 공고 기본 정보 (선택)
    private String location;
    private String experienceLevel;
    private String salary;
}