package org.zerock.nextenter.user.repository;

import org.zerock.nextenter.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 일반 로그인용 - email로만 조회
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ 소셜 로그인용 - provider + providerId로 조회
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // ✅ 추가: email + provider 조합으로 조회
    Optional<User> findByEmailAndProvider(String email, String provider);
}