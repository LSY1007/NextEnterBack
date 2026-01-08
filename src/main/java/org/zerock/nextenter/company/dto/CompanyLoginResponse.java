package org.zerock.nextenter.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLoginResponse {

    private Long companyId;
    private String token;
    private String email;
    private String name;
    private String companyName;
    private String businessNumber;
}