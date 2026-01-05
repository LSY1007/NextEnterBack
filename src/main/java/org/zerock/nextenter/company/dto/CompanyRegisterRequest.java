package org.zerock.nextenter.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyRegisterRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long companyId;

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