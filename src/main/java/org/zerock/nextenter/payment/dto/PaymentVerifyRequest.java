package org.zerock.nextenter.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyRequest {
    private String paymentId;      // 포트원 결제 고유 ID
    private String transactionId;  // 거래 ID
    private Integer amount;        // 결제 금액
    private Integer credits;       // 충전 크레딧
}