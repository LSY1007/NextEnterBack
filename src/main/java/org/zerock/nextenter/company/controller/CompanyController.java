package org.zerock.nextenter.company.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.company.dto.CompanyLoginRequest;
import org.zerock.nextenter.company.dto.CompanyLoginResponse;
import org.zerock.nextenter.company.dto.CompanyRegisterRequest;
import org.zerock.nextenter.company.dto.CompanyResponse;
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
}