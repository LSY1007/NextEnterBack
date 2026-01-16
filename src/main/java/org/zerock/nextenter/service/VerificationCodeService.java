package org.zerock.nextenter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.util.entity.VerificationCode;
import org.zerock.nextenter.util.repository.VerificationCodeRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    /**
     * 인증코드 생성 및 이메일 발송
     */
    @Transactional
    public String generateAndSendVerificationCode(String email, String userName, String type, String userType) {
        // 6자리 랜덤 숫자 생성
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 만료 시간: 10분 후
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        // DB에 저장
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .userType(userType)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        verificationCodeRepository.save(verificationCode);

        // 이메일 발송
        if ("WITHDRAWAL".equals(type)) {
            emailService.sendWithdrawalVerificationEmail(email, userName, code);
        }

        log.info("인증코드 생성 및 발송 완료: email={}, type={}, userType={}", email, type, userType);

        return code;
    }

    /**
     * 인증코드 검증
     */
    @Transactional
    public boolean verifyCode(String email, String code, String type) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndCodeAndTypeAndIsUsedFalse(email, code, type)
                .orElse(null);

        if (verificationCode == null) {
            log.warn("인증코드 찾을 수 없음: email={}, code={}, type={}", email, code, type);
            return false;
        }

        if (verificationCode.isExpired()) {
            log.warn("인증코드 만료: email={}, code={}, type={}", email, code, type);
            return false;
        }

        // 사용 처리
        verificationCode.setIsUsed(true);
        verificationCodeRepository.save(verificationCode);

        log.info("인증코드 검증 성공: email={}, type={}", email, type);
        return true;
    }

    /**
     * 만료된 인증코드 삭제
     */
    @Transactional
    public void deleteExpiredCodes() {
        verificationCodeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("만료된 인증코드 삭제 완료");
    }
}