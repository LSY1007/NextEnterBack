package org.zerock.nextenter.job.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobPostingResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long jobId;
    private Long companyId;
    private String companyName;
    private String logoUrl;
    private String title;
    private String jobCategory;
    private String requiredSkills;
    private String preferredSkills;
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer salaryMin;
    private Integer salaryMax;
    private String location;
    private String locationCity;
    private String description;
    private String thumbnailUrl;
    private String detailImageUrl;
    private LocalDate deadline;
    private String status;
    private Integer viewCount;
    private Integer applicantCount;
    private Integer bookmarkCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
