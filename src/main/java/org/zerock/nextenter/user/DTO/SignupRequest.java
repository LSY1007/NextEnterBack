package org.zerock.nextenter.user.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupRequest {

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    // ✅ 나이 추가 (선택 입력, 1~120세)
    @Schema(description = "나이", example = "25")
    @Min(value = 1, message = "나이는 1 이상이어야 합니다")
    @Max(value = 120, message = "나이는 120 이하여야 합니다")
    private Integer age;

    // ✅ 성별 추가 (선택 입력)
    @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE"})
    private String gender;

    // ✅ 주소 추가 (선택 입력)
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "상세주소", example = "3층")
    private String detailAddress;
}