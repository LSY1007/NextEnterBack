package org.zerock.nextenter.recommendation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    @NotNull(message = "이력서 ID는 필수입니다")
    private Long resumeId;

    // 선택적 필터 (AI 서버에 전달)
    private String preferredLocation;     // 선호 지역
    private List<String> preferredCompanies;  // 선호 회사 목록
    private String salaryRange;           // 희망 연봉 범위
}