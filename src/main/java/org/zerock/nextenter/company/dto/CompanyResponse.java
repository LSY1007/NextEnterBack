package org.zerock.nextenter.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private Long companyId;
    private String email;
    private String name;
    private String companyName;
    private String businessNumber;
}