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

import java.net.URI;
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

            log.info("UserInfo 추출 완료 - email: {}, name: {}", userInfo.getEmail(), userInfo.getName());

            User user = saveOrUpdate(userInfo);

            log.info("=== OAuth2 로그인 성공 ===");
            log.info("UserId: {}, Email: {}", user.getUserId(), user.getEmail());

            return new CustomOAuth2User(user, oAuth2User.getAttributes());
        } catch (Exception e) {
            log.error("=== OAuth2 로그인 실패 ===");
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
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

        // ✅ 카카오 처리 - null 체크 추가
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
                    .email(email != null ? email : "no-email@kakao.com") // 이메일 없으면 기본값
                    .name(nickname != null ? nickname : "카카오사용자")
                    .profileImage(profileImageUrl)
                    .build();
        }

        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        User user = userRepository.findByProviderAndProviderId(
                userInfo.getProvider(),
                userInfo.getProviderId()
        ).orElse(null);

        if (user == null) {
            // 신규 사용자 생성
            user = User.builder()
                    .email(userInfo.getEmail())
                    .name(userInfo.getName())
                    .profileImage(userInfo.getProfileImage())
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .isActive(true)
                    .build();

            log.info("신규 소셜 로그인 사용자 생성: provider={}, email={}",
                    userInfo.getProvider(), userInfo.getEmail());
        } else {
            // 기존 사용자 정보 업데이트
            user.setName(userInfo.getName());
            user.setProfileImage(userInfo.getProfileImage());

            log.info("기존 소셜 로그인 사용자 업데이트: provider={}, email={}",
                    userInfo.getProvider(), userInfo.getEmail());
        }

        return userRepository.save(user);
    }
}