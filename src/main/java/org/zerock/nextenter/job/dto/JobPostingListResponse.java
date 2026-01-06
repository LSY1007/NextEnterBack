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
    private String title;
    private String companyName;
    private String jobCategory;
    private String location;
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer salaryMin;
    private Integer salaryMax;
    private LocalDate deadline;
    private String status;
    private Integer viewCount;
    private Integer applicantCount;
    private LocalDateTime createdAt;
}