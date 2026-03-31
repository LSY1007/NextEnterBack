package org.zerock.nextenter.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.zerock.nextenter.security.service.CustomOAuth2User;
import org.zerock.nextenter.util.JWTUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        // application.properties에서 주입. 없으면 기본값으로 배포 URL 사용
        @Value("${app.oauth2.redirect-url:https://nextenter.store}")
        private String frontendUrl;

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
                claims.put("name", oAuth2User.getName());
                claims.put("type", "USER");

                String token = JWTUtil.generateToken(claims, 1440);

                // URL 인코딩
                String encodedEmail = URLEncoder.encode(oAuth2User.getEmail(), StandardCharsets.UTF_8);
                String encodedName = URLEncoder.encode(oAuth2User.getName(), StandardCharsets.UTF_8);

                // 프론트엔드로 리다이렉트 (토큰 포함)
                String redirectUrl = String.format(
                                "%s/oauth2/redirect?token=%s&email=%s&name=%s",
                                frontendUrl,
                                token,
                                encodedEmail,
                                encodedName);

                log.info("리다이렉트 URL: {}", redirectUrl);

                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
}
