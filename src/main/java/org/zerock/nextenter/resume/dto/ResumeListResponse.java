package org.zerock.nextenter.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeListResponse {

    private Long resumeId;
    private String title;
    private String jobCategory;
    private Boolean isMain;
    private String visibility;
    private Integer viewCount;
    private String status;
    private Boolean isIncomplete;  // 미완성 여부
    private LocalDateTime createdAt;
}