package org.zerock.nextenter.credit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.credit.dto.CreditBalanceDto;
import org.zerock.nextenter.credit.entity.Credit;
import org.zerock.nextenter.credit.repository.CreditRepository;

/**
 * 크레딧 서비스
 * - 충전, 차감, 조회 등 크레딧 관리 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {

    private final CreditRepository creditRepository;

    /**
     * 크레딧 잔액 조회
     */
    @Transactional(readOnly = true)
    public CreditBalanceDto getBalance(Long userId) {
        log.info("크레딧 잔액 조회 - userId: {}", userId);

        Credit credit = creditRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("크레딧 정보를 찾을 수 없습니다"));

        return CreditBalanceDto.fromEntity(credit);
    }

    /**
     * 크레딧 충전
     * @param userId 사용자 ID
     * @param amount 충전 금액
     * @param description 충전 사유
     * @return 충전 후 잔액 정보
     */
    @Transactional
    public CreditBalanceDto charge(Long userId, int amount, String description) {
        log.info("크레딧 충전 - userId: {}, amount: {}, description: {}", userId, amount, description);

        // 크레딧 조회 또는 생성
        Credit credit = creditRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("신규 크레딧 생성 - userId: {}", userId);
                    return Credit.builder()
                            .userId(userId)
                            .balance(0)
                            .build();
                });

        // 충전
        credit.charge(amount);
        Credit savedCredit = creditRepository.save(credit);

        log.info("크레딧 충전 완료 - userId: {}, 충전액: {}, 잔액: {}",
                userId, amount, savedCredit.getBalance());

        return CreditBalanceDto.fromEntity(savedCredit);
    }

    /**
     * 크레딧 차감
     * @param userId 사용자 ID
     * @param amount 차감 금액
     * @param description 차감 사유
     * @return 차감 후 잔액 정보
     */
    @Transactional
    public CreditBalanceDto deduct(Long userId, int amount, String description) {
        log.info("크레딧 차감 - userId: {}, amount: {}, description: {}", userId, amount, description);

        // 크레딧 조회 (비관적 락)
        Credit credit = creditRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("크레딧 정보를 찾을 수 없습니다"));

        // 잔액 확인
        if (!credit.hasEnoughBalance(amount)) {
            log.warn("크레딧 부족 - userId: {}, 현재: {}, 필요: {}",
                    userId, credit.getBalance(), amount);
            throw new IllegalStateException(
                    String.format("크레딧이 부족합니다. 현재: %d, 필요: %d",
                            credit.getBalance(), amount)
            );
        }

        // 차감
        credit.deduct(amount);
        Credit savedCredit = creditRepository.save(credit);

        log.info("크레딧 차감 완료 - userId: {}, 차감액: {}, 잔액: {}",
                userId, amount, savedCredit.getBalance());

        return CreditBalanceDto.fromEntity(savedCredit);
    }

    /**
     * 크레딧 충분 여부 확인
     * @param userId 사용자 ID
     * @param requiredAmount 필요 금액
     * @return 충분 여부
     */
    @Transactional(readOnly = true)
    public boolean hasEnoughCredit(Long userId, int requiredAmount) {
        return creditRepository.findByUserId(userId)
                .map(credit -> credit.hasEnoughBalance(requiredAmount))
                .orElse(false);
    }

    /**
     * 크레딧 초기화 (테스트용)
     * @param userId 사용자 ID
     */
    @Transactional
    public void resetCredit(Long userId) {
        log.info("크레딧 초기화 - userId: {}", userId);

        creditRepository.findByUserId(userId)
                .ifPresent(credit -> {
                    credit.setBalance(0);
                    creditRepository.save(credit);
                });
    }
}