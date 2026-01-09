package org.zerock.nextenter.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zerock.nextenter.resume.entity.Portfolio;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDto {

    private Long portfolioId;
    private Long resumeId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String description;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static PortfolioDto fromEntity(Portfolio portfolio) {
        return PortfolioDto.builder()
                .portfolioId(portfolio.getPortfolioId())
                .resumeId(portfolio.getResume().getResumeId())
                .fileName(portfolio.getFileName())
                .filePath(portfolio.getFilePath())
                .fileType(portfolio.getFileType())
                .fileSize(portfolio.getFileSize())
                .description(portfolio.getDescription())
                .displayOrder(portfolio.getDisplayOrder())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}