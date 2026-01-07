package org.zerock.nextenter.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.zerock.nextenter.security.service.CustomOAuth2User;
import org.zerock.nextenter.util.JWTUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        log.info("OAuth2 로그인 성공 - userId: {}, email: {}",
                oAuth2User.getUserId(), oAuth2User.getEmail());

        // JWT 토큰 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", oAuth2User.getUserId());
        claims.put("email", oAuth2User.getEmail());
        claims.put("type", "USER");

        String token = JWTUtil.generateToken(claims, 60);

        // 프론트엔드로 리다이렉트 (토큰 포함)
        String redirectUrl = String.format(
                "http://localhost:3000/oauth2/redirect?token=%s&email=%s&name=%s",
                token,
                oAuth2User.getEmail(),
                oAuth2User.getName()
        );

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}