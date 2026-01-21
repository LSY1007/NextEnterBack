package org.zerock.nextenter.ai.resume.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// [핵심 1] 파이썬에서 우리가 모르는 필드를 더 보내도 에러 내지 말라는 설정
@JsonIgnoreProperties(ignoreUnknown = true) 
public class AiRecommendResponse {

    private Long recommendId; 
    
    @JsonProperty("resume_id")
    private String resumeId;
    
    // [수정] 누락되었던 userId 필드 추가 완료
    private Long userId;
    
    @JsonProperty("target_role")
    private String targetRole;

    @JsonProperty("grade")
    private String grade;

    @JsonProperty("score")
    private Double score;

    @JsonProperty("ai_feedback")
    private String aiReport;

    @JsonProperty("recommendations")
    private List<CompanyRecommend> companies;

    private LocalDateTime createdAt;

    // 내부 클래스: 추천 기업 상세
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyRecommend {
        @JsonProperty("company_name")
        private String companyName;
        
        @JsonProperty("match_score")
        private Double matchScore;
        
        @JsonProperty("tier")
        private String tier;
        
        @JsonProperty("match_type")
        private String matchType;
        
        @JsonProperty("match_level")
        private String matchLevel;

        @JsonProperty("tech_stack")
        private List<String> techStack;

        @JsonProperty("missing_skills")
        private List<String> missingSkills;
        
        @JsonProperty("is_exact_match")
        private Boolean isExactMatch;
        
        // 프론트 호환용 (필요시 사용)
        public String getRole() { return matchType; }
        public Double getScore() { return matchScore; }
    }
}