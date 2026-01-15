package org.zerock.nextenter.credit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 크레딧 엔티티
 * - 사용자별 크레딧 잔액 관리
 */
@Entity
@Table(name = "credit")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_id")
    private Long creditId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 크레딧 충전
     */
    public void charge(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다");
        }
        this.balance += amount;
    }

    /**
     * 크레딧 차감
     */
    public void deduct(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다");
        }
        if (this.balance < amount) {
            throw new IllegalStateException(
                    String.format("크레딧이 부족합니다. 현재: %d, 필요: %d", this.balance, amount)
            );
        }
        this.balance -= amount;
    }

    /**
     * 잔액 확인
     */
    public boolean hasEnoughBalance(int amount) {
        return this.balance >= amount;
    }
}