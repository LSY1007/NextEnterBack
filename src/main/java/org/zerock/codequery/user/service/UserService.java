package org.zerock.codequery.user.service;

import org.zerock.codequery.user.DTO.SignupRequest;
import org.zerock.codequery.user.DTO.SignupResponse;
import org.zerock.codequery.user.entity.User;
import org.zerock.codequery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // UserType 변환
        User.UserType userType = User.UserType.valueOf(request.getUserType().toUpperCase());

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .userType(userType)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: email={}", savedUser.getEmail());

        // Response 생성
        return SignupResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .userType(savedUser.getUserType().name())
                .build();
    }
}