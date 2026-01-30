package org.zerock.nextenter.security.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;
import org.zerock.nextenter.util.JWTUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 로컬 개발 환경용 목(Mock) 로그인 컨트롤러
 * - OAuth 인증 없이 특정 이메일로 즉시 로그인 처리
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Slf4j
// @Profile("local")
public class LocalDevLoginController {

    private final UserRepository userRepository;

    @GetMapping("/login")
    public RedirectView localLogin(@RequestParam(defaultValue = "test@example.com") String email) {
        log.info("🚀 로컬 개발용 로그인 시도: email={}", email);

        // 1. 유저 조회 또는 생성
        User user = userRepository.findByEmailAndProvider(email, "LOCAL_DEV")
                .orElseGet(() -> {
                    log.info("🆕 신규 로컬 개발 유저 생성: {}", email);
                    return userRepository.save(User.builder()
                            .email(email)
                            .name("개발자_" + email.split("@")[0])
                            .provider("LOCAL_DEV")
                            .providerId("DEV_" + System.currentTimeMillis())
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .lastLoginAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());
                });

        // 2. JWT 토큰 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("type", "USER");

        String token = JWTUtil.generateToken(claims, 1440); // 24시간 유효

        // 3. 인코딩 및 리다이렉트 (OAuth2SuccessHandler의 로직과 동일)
        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
        String encodedName = URLEncoder.encode(user.getName(), StandardCharsets.UTF_8);

        String redirectUrl = String.format(
                "http://localhost:5173/oauth2/redirect?token=%s&email=%s&name=%s",
                token, encodedEmail, encodedName);

        log.info("✅ 로컬 로그인 성공! 리다이렉트 주소: {}", redirectUrl);
        return new RedirectView(redirectUrl);
    }
}
