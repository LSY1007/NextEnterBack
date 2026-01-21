package org.zerock.nextenter.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileDTO {
    // 기본 정보 (DB 실제 컬럼)
    private Long companyId;
    private String companyName;      // company.company_name
    private String businessNumber;    // company.business_number
    private String email;             // company.email
    
    // 기업 상세 정보 (DB 실제 컬럼)
    private String industry;          // company.industry
    private Integer employeeCount;    // company.employee_count
    private String logoUrl;           // company.logo_url
    private String website;           // company.website
    private String address;           // company.address
    private String description;       // company.description
    private Boolean isActive;         // company.is_active
    
    // 담당자 정보 (DB 실제 컬럼)
    private String managerName;       // company.name
    private String managerPhone;      // company.phone
    
    // 프론트엔드 편의를 위한 추가 필드 (계산 또는 매핑)
    private String companySize;       // employeeCount 기반 변환

    // 기존 필드들 아래에 추가
    private String ceoName;
    private String shortIntro;
    private String snsUrl;
    private String detailAddress;
    private String managerDepartment; // 진규 - 기업회원 마이페이지
}
