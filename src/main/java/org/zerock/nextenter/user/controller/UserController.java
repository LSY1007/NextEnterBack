package org.zerock.nextenter.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.user.DTO.LoginRequest;
import org.zerock.nextenter.user.DTO.LoginResponse;
import org.zerock.nextenter.user.DTO.SignupRequest;
import org.zerock.nextenter.user.DTO.SignupResponse;
import org.zerock.nextenter.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "일반 사용자 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            SignupResponse result = userService.signup(request);
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("data", result);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("회원가입 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "일반 사용자 로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            LoginResponse result = userService.login(request);
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("data", result);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("로그인 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @Operation(summary = "일반 사용자 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃 되었습니다.");
        return ResponseEntity.ok(response);
    }
}