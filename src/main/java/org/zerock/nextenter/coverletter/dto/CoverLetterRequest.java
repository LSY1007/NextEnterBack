package org.zerock.nextenter.coverletter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @Size(max = 50, message = "직무는 50자 이하여야 합니다")
    private String jobCategory;

    @Size(max = 100, message = "목표 회사명은 100자 이하여야 합니다")
    private String targetCompany;

    private String content;
}