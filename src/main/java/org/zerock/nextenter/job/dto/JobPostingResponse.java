package org.zerock.nextenter.job.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobPostingResponse {

    private Long jobId;
    private Long companyId;
    private String companyName;  // Company 조인 시 사용
    private String logoUrl;
    private String title;
    private String jobCategory;

    // 스킬 (JSON 문자열)
    private String requiredSkills;
    private String preferredSkills;

    // 경력
    private Integer experienceMin;
    private Integer experienceMax;

    // 연봉
    private Integer salaryMin;
    private Integer salaryMax;

    private String location;
    private String description;
    
    // 이미지 URL
    private String thumbnailUrl;
    private String detailImageUrl;
    
    private LocalDate deadline;

    // 상태 및 통계
    private String status;
    private Integer viewCount;
    private Integer applicantCount;
    private Integer bookmarkCount;

    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}