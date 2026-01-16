package org.zerock.nextenter.user.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "인증코드는 필수입니다")
    private String verificationCode;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    private String newPassword;
}