package org.zerock.nextenter.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequest {

    @NotBlank(message = "이력서 제목은 필수입니다")
    private String title;

    private String jobCategory;

    // JSON 문자열로 전달될 섹션 데이터
    private String sections;  // structuredData로 저장됨

    private String status;  // DRAFT, COMPLETED
}