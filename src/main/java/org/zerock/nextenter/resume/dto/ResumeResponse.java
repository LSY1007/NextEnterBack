package org.zerock.nextenter.resume.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeResponse {

    private Long resumeId;
    private String title;
    private String jobCategory;

    // AI 처리 결과 (나중에 사용)
    private String extractedText;
    private String structuredData;
    private String skills;
    private String resumeRecommend;

    // 파일 정보
    private String filePath;
    private String fileType;

    // 메타 정보
    private Boolean isMain;
    private String visibility;
    private Integer viewCount;
    private String status;

    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}