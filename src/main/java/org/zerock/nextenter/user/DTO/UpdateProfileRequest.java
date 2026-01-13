package org.zerock.nextenter.user.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "나이", example = "25")
    @Min(value = 1, message = "나이는 1 이상이어야 합니다")
    @Max(value = 120, message = "나이는 120 이하여야 합니다")
    private Integer age;

    @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

    @Schema(description = "자기소개", example = "안녕하세요. 저는...")
    private String bio;
}