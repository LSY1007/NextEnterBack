package org.zerock.nextenter.credit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zerock.nextenter.credit.entity.Credit;

import java.time.LocalDateTime;

/**
 * 크레딧 잔액 조회 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBalanceDto {

    private Long userId;
    private Integer balance;
    private LocalDateTime updatedAt;

    /**
     * Entity to DTO
     */
    public static CreditBalanceDto fromEntity(Credit credit) {
        return CreditBalanceDto.builder()
                .userId(credit.getUserId())
                .balance(credit.getBalance())
                .updatedAt(credit.getUpdatedAt())
                .build();
    }

    /**
     * 잔액 포맷팅
     */
    public String getFormattedBalance() {
        return String.format("%,d 크레딧", balance);
    }
}