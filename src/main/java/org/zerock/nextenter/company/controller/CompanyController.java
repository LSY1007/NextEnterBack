package org.zerock.nextenter.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.company.dto.*;
import org.zerock.nextenter.company.service.CompanyService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "기업 회원가입")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerCompany(@Valid @RequestBody CompanyRegisterRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            CompanyResponse result = companyService.registerCompany(request);
            response.put("success", true);
            response.put("message", "기업 회원가입이 완료되었습니다.");
            response.put("data", result);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("기업 회원가입 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "기업 로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody CompanyLoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            CompanyLoginResponse result = companyService.login(request);
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("data", result);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("기업 로그인 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "기업 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃 되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 기업 회원탈퇴 요청 (인증코드 발송)
     */
    @Operation(summary = "기업 회원탈퇴 요청")
    @PostMapping("/{companyId}/withdrawal/request")
    public ResponseEntity<Map<String, Object>> requestWithdrawal(@PathVariable Long companyId) {
        Map<String, Object> response = new HashMap<>();

        try {
            companyService.requestWithdrawal(companyId);
            response.put("success", true);
            response.put("message", "인증코드가 이메일로 발송되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("기업 회원탈퇴 요청 실패", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 기업 회원탈퇴 실행 (인증코드 확인)
     */
    @Operation(summary = "기업 회원탈퇴 실행")
    @PostMapping("/{companyId}/withdrawal")
    public ResponseEntity<Map<String, Object>> withdrawal(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyWithdrawalRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            companyService.withdrawal(companyId, request.getVerificationCode());
            response.put("success", true);
            response.put("message", "회원탈퇴가 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("기업 회원탈퇴 실패", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 기업 프로필 조회
     */
    @Operation(summary = "기업 프로필 조회")
    @GetMapping("/{companyId}/profile")
    public ResponseEntity<Map<String, Object>> getCompanyProfile(@PathVariable Long companyId) {
        Map<String, Object> response = new HashMap<>();

        try {
            CompanyProfileDTO profile = companyService.getCompanyProfile(companyId);
            response.put("success", true);
            response.put("data", profile); // 프론트에서는 response.data.data로 접근해야 함
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("기업 프로필 조회 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 기업 프로필 수정 (여기가 없어서 아까 저장이 안 된 겁니다!)
     */
    @Operation(summary = "기업 프로필 수정")
    @PutMapping("/{companyId}/profile")
    public ResponseEntity<Map<String, Object>> updateCompanyProfile(
            @PathVariable Long companyId,
            @RequestBody CompanyProfileDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            CompanyProfileDTO updated = companyService.updateCompanyProfile(companyId, dto);
            response.put("success", true);
            response.put("message", "기업 정보가 저장되었습니다.");
            response.put("data", updated);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("기업 프로필 수정 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    /*
     * 비밀번호 변경
     * PUT /api/company/{companyId}/password
     */
    @Operation(summary = "기업 비밀번호 변경")
    @PostMapping("/{companyId}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long companyId,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                response.put("success", false);
                response.put("message", "현재 비밀번호와 새 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            companyService.changePassword(companyId, currentPassword, newPassword);
            response.put("success", true);
            response.put("message", "비밀번호가 변경되었습니다.");
            return ResponseEntity.ok(response);

        }  catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("비밀변경 변경 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}