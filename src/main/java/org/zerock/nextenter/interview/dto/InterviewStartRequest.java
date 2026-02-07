package org.zerock.nextenter.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStartRequest {

    @Schema(description = "이력서 ID", example = "1")
    @NotNull(message = "이력서 ID는 필수입니다")
    private Long resumeId;

    @Schema(description = "직무 분류", example = "백엔드 개발")
    @NotBlank(message = "직무 분류는 필수입니다")
    private String jobCategory;

    @Schema(description = "난이도", example = "JUNIOR", allowableValues = { "JUNIOR", "SENIOR" })
    @NotBlank(message = "난이도는 필수입니다")
    @Pattern(regexp = "^(JUNIOR|SENIOR)$", message = "난이도는 JUNIOR 또는 SENIOR만 가능합니다")
    private String difficulty;

    @Schema(description = "총 턴 수 (기본값: 5)", example = "5")
    private Integer totalTurns;

    @Schema(description = "포트폴리오 텍스트 (선택)", example = "GitHub: https://github.com/user, 프로젝트: AI 챗봇 개발")
    private String portfolioText;
}