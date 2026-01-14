package org.zerock.nextenter.resume.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포트폴리오 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUpdateRequest {

    /**
     * 포트폴리오 설명
     */
    @Size(max = 1000, message = "설명은 최대 1000자까지 입력 가능합니다")
    private String description;

    /**
     * 표시 순서 (0부터 시작)
     */
    private Integer displayOrder;
}
