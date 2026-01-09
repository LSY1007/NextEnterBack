package org.zerock.nextenter.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 포트폴리오 업로드 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUploadResponse {

    private Long portfolioId;
    private Long resumeId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String message;

    /**
     * 성공 응답 생성
     */
    public static PortfolioUploadResponse success(Long portfolioId, Long resumeId, String fileName,
                                                  String fileType, Long fileSize) {
        return PortfolioUploadResponse.builder()
                .portfolioId(portfolioId)
                .resumeId(resumeId)
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .message("포트폴리오 파일이 업로드되었습니다")
                .build();
    }
}