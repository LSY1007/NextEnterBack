package org.zerock.nextenter.ai.resume.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendResponse {

    private Long recommendId;
    private Long resumeId;
    private Long userId;
    
    private List<CompanyRecommend> companies;
    private String aiReport;
    private LocalDateTime createdAt;

    // 추천 회사 정보
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyRecommend {
        private String companyName;
        private String role;
        private Double score;
        private String matchLevel;  // BEST, GOOD, NORMAL 등
        private Boolean isExactMatch;
    }
}
