package org.zerock.nextenter.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioListResponse {

    private Long resumeId;
    private Long totalCount;
    private Long totalFileSize;
    private List<PortfolioDto> portfolios;

    public String getFormattedTotalFileSize() {
        if (totalFileSize == null) return "0 B";

        long bytes = totalFileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}