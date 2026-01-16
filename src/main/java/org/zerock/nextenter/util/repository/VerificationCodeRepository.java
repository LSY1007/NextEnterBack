package org.zerock.nextenter.util.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.nextenter.util.entity.VerificationCode;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByEmailAndCodeAndTypeAndIsUsedFalse(
            String email,
            String code,
            String type
    );

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}