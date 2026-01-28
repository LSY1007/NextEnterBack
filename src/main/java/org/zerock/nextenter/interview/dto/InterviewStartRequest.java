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

    // --- Proxy/Pass-through Fields (Optional) ---
    @Schema(description = "이력서 내용 (프론트엔드 전달용, 없을 시 DB 조회)", hidden = true)
    private java.util.Map<String, Object> resumeContent;

    @Schema(description = "포트폴리오 내용 (프론트엔드 전달용)", hidden = true)
    private java.util.Map<String, Object> portfolio;

    @Schema(description = "포트폴리오 파일 경로 목록 (프론트엔드 전달용)", hidden = true)
    private java.util.List<String> portfolioFiles;
}