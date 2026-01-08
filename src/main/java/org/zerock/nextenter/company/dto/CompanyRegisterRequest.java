package org.zerock.nextenter.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRegisterRequest {

    // 계정 정보
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotBlank(message = "담당자 이름은 필수입니다")
    private String name;

    private String phone;

    // 기업 정보
    @NotBlank(message = "사업자등록번호는 필수입니다")
    private String businessNumber;

    @NotBlank(message = "기업명은 필수입니다")
    private String companyName;

    private String industry;
    private Integer employeeCount;
    private String address;
    private String logoUrl;
    private String website;
    private String description;
}