package org.zerock.nextenter.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.service.VerificationCodeService;
import org.zerock.nextenter.user.DTO.*;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;
import org.zerock.nextenter.util.JWTUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;

    // ✅ 기존 설정 사용
    @Value("${file.upload-dir}")
    private String uploadBaseDir;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 성별 변환
        User.Gender gender = null;
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            try {
                gender = User.Gender.valueOf(request.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 성별입니다. MALE, FEMALE 중 하나를 선택하세요.");
            }
        }

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .age(request.getAge())
                .gender(gender)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: email={}, age={}, gender={}",
                savedUser.getEmail(), savedUser.getAge(), savedUser.getGender());

        return SignupResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .age(savedUser.getAge())
                .gender(savedUser.getGender() != null ? savedUser.getGender().name() : null)
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

        String token = JWTUtil.generateToken(claims, 180);

        log.info("로그인 완료: email={}", user.getEmail());

        return LoginResponse.builder()
                .userId(user.getUserId())
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .age(user.getAge())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 정보 업데이트
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone());
        }

        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }

        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 성별입니다. MALE, FEMALE, OTHER 중 하나를 선택하세요.");
            }
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio().trim().isEmpty() ? null : request.getBio());
        }

        User updatedUser = userRepository.save(user);

        return UserProfileResponse.builder()
                .userId(updatedUser.getUserId())
                .email(updatedUser.getEmail())
                .name(updatedUser.getName())
                .phone(updatedUser.getPhone())
                .age(updatedUser.getAge())
                .gender(updatedUser.getGender() != null ? updatedUser.getGender().name() : null)
                .profileImage(updatedUser.getProfileImage())
                .bio(updatedUser.getBio())
                .provider(updatedUser.getProvider())
                .createdAt(updatedUser.getCreatedAt())
                .build();
    }

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ✅ profile-images 하위 폴더 생성
        File uploadDir = new File(uploadBaseDir + "/profile-images");

        // 디렉토리가 없으면 생성
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new IOException("업로드 디렉토리 생성 실패: " + uploadDir.getAbsolutePath());
            }
            log.info("업로드 디렉토리 생성: {}", uploadDir.getAbsolutePath());
        }

        log.info("업로드 디렉토리: {}", uploadDir.getAbsolutePath());

        // 기존 이미지 삭제
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                String oldFilename = user.getProfileImage().substring(user.getProfileImage().lastIndexOf("/") + 1);
                File oldFile = new File(uploadDir, oldFilename);
                if (oldFile.exists() && oldFile.delete()) {
                    log.info("기존 프로필 이미지 삭제: {}", oldFile.getAbsolutePath());
                }
            } catch (Exception e) {
                log.warn("기존 프로필 이미지 삭제 실패: {}", e.getMessage());
            }
        }

        // 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        File destFile = new File(uploadDir, filename);
        file.transferTo(destFile);

        // ✅ DB에는 프론트엔드에서 사용할 상대 경로 저장
        String imageUrl = "/images/profile-images/" + filename;
        user.setProfileImage(imageUrl);
        userRepository.save(user);

        log.info("프로필 이미지 업로드 완료: userId={}, path={}, url={}",
                userId, destFile.getAbsolutePath(), imageUrl);

        return imageUrl;
    }

    @Transactional
    public User getOrCreateOAuthUser(
            String email,
            String name,
            String provider,
            String providerId
    ) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    // 기존 회원 → OAuth 정보만 갱신
                    user.setProvider(provider);
                    user.setProviderId(providerId);
                    user.setLastLoginAt(LocalDateTime.now());
                    return user;
                })
                .orElseGet(() -> {
                    // 신규 OAuth 회원
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .provider(provider)
                            .providerId(providerId)
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });
    }

    /**
     * 회원탈퇴 요청 (인증코드 발송)
     */
    @Transactional
    public void requestWithdrawal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 인증코드 생성 및 이메일 발송
        verificationCodeService.generateAndSendVerificationCode(
                user.getEmail(),
                user.getName(),
                "WITHDRAWAL",
                "USER"
        );

        log.info("회원탈퇴 인증코드 발송: userId={}, email={}", userId, user.getEmail());
    }

    /**
     * 회원탈퇴 실행 (인증코드 확인 후 삭제)
     */
    @Transactional
    public void withdrawal(Long userId, String verificationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 인증코드 검증
        boolean isValid = verificationCodeService.verifyCode(
                user.getEmail(),
                verificationCode,
                "WITHDRAWAL"
        );

        if (!isValid) {
            throw new IllegalArgumentException("인증코드가 유효하지 않거나 만료되었습니다.");
        }

        // 회원 삭제 (CASCADE로 연관 데이터도 함께 삭제됨)
        userRepository.delete(user);

        log.info("회원탈퇴 완료: userId={}, email={}", userId, user.getEmail());
    }
}