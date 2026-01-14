package org.zerock.nextenter.credit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 크레딧 충전 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditChargeRequest {

    @NotNull(message = "충전 금액은 필수입니다")
    @Min(value = 100, message = "최소 충전 금액은 100 크레딧입니다")
    private Integer amount;

    private String paymentMethod; // 결제 수단 (선택)

    private String description; // 충전 사유 (선택)
}