package org.zerock.nextenter.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.time.LocalDateTime;

/**
 * 로컬 개발 환경용 테스트 데이터 로더
 * - 애플리케이션 시작 시 테스트용 계정 자동 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
// @Profile("local") // Enable for all profiles temporarily
public class LocalDevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String testEmail = "test@test.com";
        String testPassword = "1234";

        if (!userRepository.existsByEmail(testEmail)) {
            log.info("Generating test user: email={}, password={}", testEmail, testPassword);

            User testUser = User.builder()
                    .email(testEmail)
                    .password(passwordEncoder.encode(testPassword))
                    .name("테스트유저")
                    .phone("010-1234-5678")
                    .age(25)
                    .gender(User.Gender.MALE)
                    .address("서울시 강남구")
                    .detailAddress("테스트아파트 101호")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .provider("LOCAL") // 일반 로그인 방식
                    .build();

            userRepository.save(testUser);
            log.info("Test user created successfully!");
        } else {
            log.info("Test user already exists. Skipping creation.");
        }
    }
}
