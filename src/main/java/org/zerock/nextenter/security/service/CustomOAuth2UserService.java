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
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 로그인 - provider: {}", registrationId);

        OAuth2UserInfo userInfo = extractUserInfo(registrationId, oAuth2User.getAttributes());

        User user = saveOrUpdate(userInfo);

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
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

        // ✅ 카카오 처리 추가
        if ("kakao".equals(registrationId)) {
            Long id = (Long) attributes.get("id");
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            return OAuth2UserInfo.builder()
                    .providerId(String.valueOf(id))
                    .provider("KAKAO")
                    .email((String) kakaoAccount.get("email"))
                    .name((String) profile.get("nickname"))
                    .profileImage((String) profile.get("profile_image_url"))
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