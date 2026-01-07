package org.zerock.nextenter.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.user.DTO.LoginRequest;
import org.zerock.nextenter.user.DTO.LoginResponse;
import org.zerock.nextenter.user.DTO.SignupRequest;
import org.zerock.nextenter.user.DTO.SignupResponse;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;
import org.zerock.nextenter.util.JWTUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: email={}", savedUser.getEmail());

        return SignupResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 활성화 여부 확인
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // JWT 토큰 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("type", "USER");

        String token = JWTUtil.generateToken(claims, 60);

        log.info("로그인 완료: email={}", user.getEmail());

        return LoginResponse.builder()
                .userId(user.getUserId())
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}