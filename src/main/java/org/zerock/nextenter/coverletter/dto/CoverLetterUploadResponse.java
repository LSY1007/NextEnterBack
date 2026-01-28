package org.zerock.nextenter.coverletter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterUploadResponse {
    private Long coverLetterId;
    private String title;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String message;
}