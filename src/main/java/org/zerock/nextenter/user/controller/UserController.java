package org.zerock.nextenter.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.user.DTO.*;
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

    // ✅ 아래부터 새로 추가하는 메서드들

    @Operation(summary = "사용자 프로필 조회")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            UserProfileResponse profile = userService.getUserProfile(userId);
            response.put("success", true);
            response.put("message", "프로필 조회 성공");
            response.put("data", profile);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("프로필 조회 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "사용자 프로필 수정")
    @PutMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            UserProfileResponse profile = userService.updateUserProfile(userId, request);
            response.put("success", true);
            response.put("message", "프로필 수정 성공");
            response.put("data", profile);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("프로필 수정 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Operation(summary = "프로필 이미지 업로드")
    @PostMapping("/user/{userId}/profile-image")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 파일 검증
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 파일 크기 체크 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 5MB 이하여야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 파일인지 확인
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String imageUrl = userService.uploadProfileImage(userId, file);

            Map<String, String> data = new HashMap<>();
            data.put("profileImage", imageUrl);

            response.put("success", true);
            response.put("message", "프로필 이미지 업로드 성공");
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("이미지 업로드 오류", e);
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 회원탈퇴 요청 (인증코드 발송)
     */
    @Operation(summary = "회원탈퇴 요청")
    @PostMapping("/user/{userId}/withdrawal/request")
    public ResponseEntity<Map<String, Object>> requestWithdrawal(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.requestWithdrawal(userId);
            response.put("success", true);
            response.put("message", "인증코드가 이메일로 발송되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("회원탈퇴 요청 실패", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 회원탈퇴 실행 (인증코드 확인)
     */
    @Operation(summary = "회원탈퇴 실행")
    @PostMapping("/user/{userId}/withdrawal")
    public ResponseEntity<Map<String, Object>> withdrawal(
            @PathVariable Long userId,
            @Valid @RequestBody WithdrawalRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.withdrawal(userId, request.getVerificationCode());
            response.put("success", true);
            response.put("message", "회원탈퇴가 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("회원탈퇴 실패", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}