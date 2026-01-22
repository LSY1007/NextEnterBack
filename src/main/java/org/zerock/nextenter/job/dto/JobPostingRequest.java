package org.zerock.nextenter.job.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingRequest {

    @NotBlank(message = "공고 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "직무 분류는 필수입니다.")
    private String jobCategory;

    private String requiredSkills;
    private String preferredSkills;

    private Integer experienceMin;
    private Integer experienceMax;

    private Integer salaryMin;
    private Integer salaryMax;

    private String location;
    private String description;

    // 이미지 URL
    private String thumbnailUrl;
    private String detailImageUrl;

    private LocalDate deadline;

    private String status;
}
