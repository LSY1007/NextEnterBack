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
    private Boolean isIncomplete; // 미완성 여부
    private LocalDateTime createdAt;

    // [NEW] 파일 기반 이력서 구분을 위한 필드
    private String filePath; // 업로드된 파일 경로
    private String fileType; // 파일 확장자 (pdf, docx 등)
    private Boolean isFileBased; // 파일 기반 이력서 여부 (filePath가 있으면 true)
}