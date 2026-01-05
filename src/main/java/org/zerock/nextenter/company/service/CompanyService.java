package org.zerock.nextenter.company.service;

import org.zerock.nextenter.company.dto.CompanyRegisterRequest;
import org.zerock.nextenter.company.dto.CompanyResponse;
import org.zerock.nextenter.company.entity.Company;
import org.zerock.nextenter.company.repository.CompanyRepository;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompanyResponse registerCompany(CompanyRegisterRequest request) {
        // 사용자 존재 확인
        User user = userRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 사용자 타입 확인
        if (user.getUserType() != User.UserType.COMPANY) {
            throw new IllegalArgumentException("기업 회원만 기업 정보를 등록할 수 있습니다.");
        }

        // 사업자등록번호 중복 체크
        if (companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }

        // 이미 등록된 기업 정보가 있는지 확인
        if (companyRepository.findByUser_UserId(request.getCompanyId()).isPresent()) {
            throw new IllegalArgumentException("이미 기업 정보가 등록되어 있습니다.");
        }

        // Company 생성
        Company company = Company.builder()
                .user(user)
                .businessNumber(request.getBusinessNumber())
                .companyName(request.getCompanyName())
                .industry(request.getIndustry())
                .employeeCount(request.getEmployeeCount())
                .address(request.getAddress())
                .logoUrl(request.getLogoUrl())
                .website(request.getWebsite())
                .description(request.getDescription())
                .build();

        Company savedCompany = companyRepository.save(company);
        log.info("기업 정보 등록 완료: companyName={}, userId={}", savedCompany.getCompanyName(), request.getCompanyId());

        // Response 생성
        return CompanyResponse.builder()
                .companyId(savedCompany.getCompanyId())
                .userId(savedCompany.getUser().getUserId())
                .businessNumber(savedCompany.getBusinessNumber())
                .companyName(savedCompany.getCompanyName())
                .industry(savedCompany.getIndustry())
                .employeeCount(savedCompany.getEmployeeCount())
                .address(savedCompany.getAddress())
                .logoUrl(savedCompany.getLogoUrl())
                .website(savedCompany.getWebsite())
                .description(savedCompany.getDescription())
                .build();
    }
}