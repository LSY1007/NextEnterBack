package org.zerock.nextenter.credit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.credit.entity.Credit;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {

    /**
     * 사용자 ID로 크레딧 조회
     */
    Optional<Credit> findByUserId(Long userId);

    /**
     * 사용자 ID로 크레딧 조회 (비관적 락)
     * - 동시성 제어: 여러 요청이 동시에 크레딧을 차감하는 것을 방지
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Credit c WHERE c.userId = :userId")
    Optional<Credit> findByUserIdWithLock(@Param("userId") Long userId);

    /**
     * 사용자 크레딧 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 사용자 잔액만 조회 (가벼운 쿼리)
     */
    @Query("SELECT c.balance FROM Credit c WHERE c.userId = :userId")
    Optional<Integer> findBalanceByUserId(@Param("userId") Long userId);
}