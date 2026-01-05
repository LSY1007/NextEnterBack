package org.zerock.codequery.company.dto;

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
    private Long userId;
    private String businessNumber;
    private String companyName;
    private String industry;
    private Integer employeeCount;
    private String address;
    private String logoUrl;
    private String website;
    private String description;
}