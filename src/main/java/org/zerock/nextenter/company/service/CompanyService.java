package org.zerock.nextenter.company.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.company.dto.CompanyLoginRequest;
import org.zerock.nextenter.company.dto.CompanyLoginResponse;
import org.zerock.nextenter.company.dto.CompanyProfileDTO;
import org.zerock.nextenter.company.dto.CompanyRegisterRequest;
import org.zerock.nextenter.company.dto.CompanyResponse;
import org.zerock.nextenter.company.entity.Company;
import org.zerock.nextenter.company.repository.CompanyRepository;
import org.zerock.nextenter.service.VerificationCodeService;
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
    private final JWTUtil JWTUtil;
    private final VerificationCodeService verificationCodeService;

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
                .detailAddress(request.getDetailAddress())  // ✅ 상세주소 추가
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
                request.getBusinessNumber())
                .orElseThrow(() -> new IllegalArgumentException("이메일, 비밀번호 또는 사업자등록번호가 일치하지 않습니다."));

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

        String token = JWTUtil.generateToken(claims, 1440); // 24시간

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

    /**
     * 기업 회원탈퇴 요청 (인증코드 발송)
     */
    @Transactional
    public void requestWithdrawal(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("기업을 찾을 수 없습니다."));

        // 인증코드 생성 및 이메일 발송
        verificationCodeService.generateAndSendVerificationCode(
                company.getEmail(),
                company.getName(),
                "WITHDRAWAL",
                "COMPANY");

        log.info("기업 회원탈퇴 인증코드 발송: companyId={}, email={}", companyId, company.getEmail());
    }

    /**
     * 기업 회원탈퇴 실행 (인증코드 확인 후 삭제)
     */
    @Transactional
    public void withdrawal(Long companyId, String verificationCode) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("기업을 찾을 수 없습니다."));

        // 인증코드 검증
        boolean isValid = verificationCodeService.verifyCode(
                company.getEmail(),
                verificationCode,
                "WITHDRAWAL");

        if (!isValid) {
            throw new IllegalArgumentException("인증코드가 유효하지 않거나 만료되었습니다.");
        }

        // 기업 삭제 (CASCADE로 연관 데이터도 함께 삭제됨)
        companyRepository.delete(company);

        log.info("기업 회원탈퇴 완료: companyId={}, email={}", companyId, company.getEmail());
    }

    /**
     * 기업 프로필 조회
     */
    @Transactional(readOnly = true)
    public CompanyProfileDTO getCompanyProfile(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다."));

        return CompanyProfileDTO.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .businessNumber(company.getBusinessNumber())
                .email(company.getEmail())
                .industry(company.getIndustry())
                .employeeCount(company.getEmployeeCount())
                .logoUrl(company.getLogoUrl())
                .website(company.getWebsite())
                .address(company.getAddress())
                .description(company.getDescription())
                .isActive(company.getIsActive())
                .managerName(company.getName())
                .managerPhone(company.getPhone())
                .companySize(convertEmployeeCountToSize(company.getEmployeeCount()))

                // 진규 - 기업회원 마이페이지 ✅ [추가됨] 새로 추가된 필드 매핑
                .ceoName(company.getCeoName())
                .shortIntro(company.getShortIntro())
                .snsUrl(company.getSnsUrl())
                .detailAddress(company.getDetailAddress())
                .managerDepartment(company.getManagerDepartment())
                .build();
    }

    /**
     * 기업 프로필 수정
     */
    @Transactional
    public CompanyProfileDTO updateCompanyProfile(Long companyId, CompanyProfileDTO dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다."));

        // 수정 가능한 필드만 업데이트 (DB에 있는 컨럼만)
        if (dto.getAddress() != null) {
            company.setAddress(dto.getAddress());
        }
        if (dto.getDescription() != null) {
            company.setDescription(dto.getDescription());
        }
        if (dto.getIndustry() != null) {
            company.setIndustry(dto.getIndustry());
        }
        if (dto.getEmployeeCount() != null) {
            company.setEmployeeCount(dto.getEmployeeCount());
        }
        if (dto.getLogoUrl() != null) {
            company.setLogoUrl(dto.getLogoUrl());
        }
        if (dto.getWebsite() != null) {
            company.setWebsite(dto.getWebsite());
        }
        if (dto.getManagerName() != null) {
            company.setName(dto.getManagerName());
        }
        if (dto.getManagerPhone() != null) {
            company.setPhone(dto.getManagerPhone());
        }
        // 진규 - 기업회원 마이페이지 관련
        if (dto.getCeoName() != null) {
            company.setCeoName(dto.getCeoName());
        }
        if (dto.getShortIntro() != null) {
            company.setShortIntro(dto.getShortIntro());
        }
        if (dto.getSnsUrl() != null) {
            company.setSnsUrl(dto.getSnsUrl());
        }
        if (dto.getDetailAddress() != null) {
            company.setDetailAddress(dto.getDetailAddress());
        }
        if (dto.getManagerDepartment() != null) {
            company.setManagerDepartment(dto.getManagerDepartment());
        }

        Company saved = companyRepository.save(company);
        log.info("기업 프로필 수정 완료: companyId={}", companyId);

        return getCompanyProfile(saved.getCompanyId());
    }

    /**
     * 비밀번호 변경 (현재 비밀번호 확인)
     */
    @Transactional
    public void changePassword(Long companyId, String currentPassword, String newPassword) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, company.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호로 변경
        company.setPassword(passwordEncoder.encode(newPassword));
        companyRepository.save(company);

        log.info("기업 비밀번호 변경 완료: companyId={}", companyId);
    }

    /**
     * 직원 수를 기업 규모 문자열로 변환
     */
    private String convertEmployeeCountToSize(Integer employeeCount) {
        if (employeeCount == null)
            return "";
        if (employeeCount <= 10)
            return "1-10명";
        if (employeeCount <= 50)
            return "11-50명";
        if (employeeCount <= 200)
            return "51-200명";
        if (employeeCount <= 500)
            return "201-500명";
        if (employeeCount <= 1000)
            return "501-1000명";
        return "1000명 이상";
    }
}