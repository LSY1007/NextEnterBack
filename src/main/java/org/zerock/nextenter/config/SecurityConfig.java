package org.zerock.nextenter.config;

import org.zerock.nextenter.security.filter.JWTCheckFilter;
import org.zerock.nextenter.security.handler.OAuth2SuccessHandler;
import org.zerock.nextenter.security.service.CustomOAuth2UserService;
import org.zerock.nextenter.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("============== Security Config 적용 중 ==============");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ 인증 실패 시 401 에러 반환 (리다이렉션 방지)
                .exceptionHandling(e ->
                        e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JWTCheckFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요 - 공개 API
                        .requestMatchers("/api/company/login", "/api/company/register").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/resume/**").permitAll()
                        .requestMatchers("/api/jobs/**").permitAll()
                        .requestMatchers("/api/matching/**").permitAll()
                        .requestMatchers("/api/interview/**").permitAll()
                        .requestMatchers("/api/credit/**").permitAll()
                        .requestMatchers("/api/recommendations/**").permitAll()
                        .requestMatchers("/api/bookmarks/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()

                        // 🔔 WebSocket 알림 시스템
                        .requestMatchers("/ws/**").permitAll() // WebSocket 엔드포인트
                        .requestMatchers("/api/notifications/**").permitAll() // 알림 API
                        .requestMatchers("/api/notification-settings/**").permitAll() // 알림 설정 API

                        // 기타 공개 리소스
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/login/oauth2/code/**").permitAll()

                        // 인증 필요
                        .requestMatchers("/api/company/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ allowedOriginPatterns 사용 (allowCredentials와 호환)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000" // 추가 포트도 지원
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // ✅ true + allowedOriginPatterns 조합 가능
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}