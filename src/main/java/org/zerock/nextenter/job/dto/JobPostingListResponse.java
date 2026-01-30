package org.zerock.nextenter.job.dto;

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
public class JobPostingListResponse {

    private Long jobId;
    private Long companyId;
    private String title;
    private String companyName;
    private String logoUrl;
    private String thumbnailUrl;
    private String detailImageUrl; // ✅ 추가: 상세 이미지 URL
    private String jobCategory;
    private String location;
    private String locationCity; // 시/도 정보 (필터링용)
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer salaryMin;
    private Integer salaryMax;
    private LocalDate deadline;
    private String status;
    private Integer viewCount;
    private Integer applicantCount;
    private Integer bookmarkCount;
    private LocalDateTime createdAt;
}