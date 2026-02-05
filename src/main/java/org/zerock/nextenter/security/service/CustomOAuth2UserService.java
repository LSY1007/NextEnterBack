package org.zerock.nextenter.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.zerock.nextenter.user.DTO.OAuth2UserInfo;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("=== OAuth2 로그인 시작 ===");
            log.info("Provider: {}", registrationId);
            log.info("Attributes: {}", oAuth2User.getAttributes());

            OAuth2UserInfo userInfo = extractUserInfo(registrationId, oAuth2User.getAttributes());

            log.info("UserInfo 추출 완료 - email: {}, name: {}, provider: {}",
                    userInfo.getEmail(), userInfo.getName(), userInfo.getProvider());

            User user = saveOrUpdate(userInfo);

            log.info("=== OAuth2 로그인 성공 ===");
            log.info("UserId: {}, Email: {}, Provider: {}",
                    user.getUserId(), user.getEmail(), user.getProvider());

            return new CustomOAuth2User(user, oAuth2User.getAttributes());
        } catch (Exception e) {
            log.error("=== OAuth2 로그인 실패 ===");
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        // ✅ 네이버
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return OAuth2UserInfo.builder()
                    .providerId((String) response.get("id"))
                    .provider("NAVER")
                    .email((String) response.get("email"))
                    .name((String) response.get("name"))
                    .profileImage((String) response.get("profile_image"))
                    .build();
        }

        // ✅ 카카오
        if ("kakao".equals(registrationId)) {
            log.info("카카오 attributes: {}", attributes);

            Long id = (Long) attributes.get("id");
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

            if (kakaoAccount == null) {
                log.error("kakao_account가 null입니다");
                throw new OAuth2AuthenticationException("카카오 계정 정보를 가져올 수 없습니다");
            }

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            if (profile == null) {
                log.error("profile이 null입니다");
                throw new OAuth2AuthenticationException("카카오 프로필 정보를 가져올 수 없습니다");
            }

            String email = (String) kakaoAccount.get("email");
            String nickname = (String) profile.get("nickname");
            String profileImageUrl = (String) profile.get("profile_image_url");

            log.info("카카오 사용자 정보 - id: {}, email: {}, nickname: {}", id, email, nickname);

            return OAuth2UserInfo.builder()
                    .providerId(String.valueOf(id))
                    .provider("KAKAO")
                    .email(email != null ? email : "no-email@kakao.com")
                    .name(nickname != null ? nickname : "카카오사용자")
                    .profileImage(profileImageUrl)
                    .build();
        }

        // ✅ 구글 (새로 추가)
        if ("google".equals(registrationId)) {
            log.info("구글 attributes: {}", attributes);

            String sub = (String) attributes.get("sub"); // 구글 고유 ID
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String picture = (String) attributes.get("picture");

            log.info("구글 사용자 정보 - sub: {}, email: {}, name: {}", sub, email, name);

            return OAuth2UserInfo.builder()
                    .providerId(sub)
                    .provider("GOOGLE")
                    .email(email != null ? email : "no-email@google.com")
                    .name(name != null ? name : "구글사용자")
                    .profileImage(picture)
                    .build();
        }

        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    /**
     * 각 소셜 계정을 독립적으로 관리
     * - email + provider 조합으로 사용자 조회
     * - 같은 이메일이어도 provider가 다르면 별도 계정 생성
     */
    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        // 1️⃣ email + provider 조합으로 조회
        User user = userRepository
                .findByEmailAndProvider(userInfo.getEmail(), userInfo.getProvider())
                .orElse(null);

        if (user != null) {
            // 기존 소셜 계정 존재 - 정보 업데이트
            user.setName(userInfo.getName());
            user.setProfileImage(userInfo.getProfileImage());
            user.setLastLoginAt(java.time.LocalDateTime.now());

            log.info("✅ 기존 소셜 계정 로그인: email={}, provider={}",
                    userInfo.getEmail(), userInfo.getProvider());

            return userRepository.save(user);
        }

        // 2️⃣ 새로운 소셜 계정 생성
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .profileImage(userInfo.getProfileImage())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .isActive(true)
                .createdAt(java.time.LocalDateTime.now())
                .lastLoginAt(java.time.LocalDateTime.now())
                .build();

        log.info("✅ 신규 소셜 계정 생성: email={}, provider={}",
                userInfo.getEmail(), userInfo.getProvider());

        return userRepository.save(newUser);
    }
}