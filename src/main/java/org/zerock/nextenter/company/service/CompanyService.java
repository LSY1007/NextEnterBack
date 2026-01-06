package org.zerock.nextenter.company.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.company.dto.CompanyLoginRequest;
import org.zerock.nextenter.company.dto.CompanyLoginResponse;
import org.zerock.nextenter.company.dto.CompanyRegisterRequest;
import org.zerock.nextenter.company.dto.CompanyResponse;
import org.zerock.nextenter.company.entity.Company;
import org.zerock.nextenter.company.repository.CompanyRepository;
import org.zerock.nextenter.util.JWTUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Transactional
    public CompanyResponse registerCompany(CompanyRegisterRequest request) {
        // 이메일 중복 체크
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 사업자등록번호 중복 체크
        if (companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }

        // Company 생성
        Company company = Company.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
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
        log.info("기업 회원가입 완료: email={}, companyName={}", savedCompany.getEmail(), savedCompany.getCompanyName());

        return CompanyResponse.builder()
                .companyId(savedCompany.getCompanyId())
                .email(savedCompany.getEmail())
                .name(savedCompany.getName())
                .companyName(savedCompany.getCompanyName())
                .businessNumber(savedCompany.getBusinessNumber())
                .build();
    }

    @Transactional
    public CompanyLoginResponse login(CompanyLoginRequest request) {
        // 이메일과 사업자등록번호로 기업 조회
        Company company = companyRepository.findByEmailAndBusinessNumber(
                request.getEmail(),
                request.getBusinessNumber()
        ).orElseThrow(() -> new IllegalArgumentException("이메일, 비밀번호 또는 사업자등록번호가 일치하지 않습니다."));

        // 활성화 여부 확인
        if (!company.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), company.getPassword())) {
            throw new IllegalArgumentException("이메일, 비밀번호 또는 사업자등록번호가 일치하지 않습니다.");
        }

        // 마지막 로그인 시간 업데이트
        company.setLastLoginAt(LocalDateTime.now());
        companyRepository.save(company);

        // JWT 토큰 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("companyId", company.getCompanyId());
        claims.put("email", company.getEmail());
        claims.put("businessNumber", company.getBusinessNumber());
        claims.put("type", "COMPANY");

        String token = jwtUtil.generateToken(claims, 60);

        log.info("기업 로그인 완료: email={}, companyName={}", company.getEmail(), company.getCompanyName());

        return CompanyLoginResponse.builder()
                .companyId(company.getCompanyId())
                .token(token)
                .email(company.getEmail())
                .name(company.getName())
                .companyName(company.getCompanyName())
                .businessNumber(company.getBusinessNumber())
                .build();
    }
}