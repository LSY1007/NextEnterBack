package org.zerock.nextenter.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.zerock.nextenter.credit.service.CreditService;
import org.zerock.nextenter.payment.dto.PaymentVerifyRequest;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Payment", description = "결제 검증 API")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final CreditService creditService;
    private final RestTemplate restTemplate;

    @Value("${portone.api.secret:}") // application.properties에서 읽음
    private String portoneApiSecret;

    @Operation(summary = "포트원 결제 검증 및 크레딧 충전")
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId,
            @RequestBody PaymentVerifyRequest request
    ) {
        log.info("결제 검증 시작 - userId: {}, paymentId: {}", userId, request.getPaymentId());

        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ 1. 포트원 API로 결제 정보 조회
            String portoneUrl = "https://api.portone.io/payments/" + request.getPaymentId();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "PortOne " + portoneApiSecret);

            org.springframework.http.HttpEntity<String> entity =
                    new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<Map> portoneResponse = restTemplate.exchange(
                    portoneUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> paymentData = portoneResponse.getBody();

            if (paymentData == null) {
                throw new IllegalStateException("결제 정보를 가져올 수 없습니다");
            }

            // ✅ 2. 결제 상태 확인
            String status = (String) paymentData.get("status");
            if (!"PAID".equals(status)) {
                throw new IllegalStateException("결제가 완료되지 않았습니다: " + status);
            }

            // ✅ 3. 결제 금액 검증
            Map<String, Object> amountData = (Map<String, Object>) paymentData.get("amount");
            Integer paidAmount = (Integer) amountData.get("total");

            if (!request.getAmount().equals(paidAmount)) {
                throw new IllegalStateException(
                        String.format("결제 금액이 일치하지 않습니다. 요청: %d, 실제: %d",
                                request.getAmount(), paidAmount)
                );
            }

            // ✅ 4. 크레딧 충전
            creditService.charge(
                    userId,
                    request.getCredits(),
                    "카카오페이 결제 (결제ID: " + request.getPaymentId() + ")"
            );

            response.put("success", true);
            response.put("message", "결제가 완료되었습니다");
            response.put("credits", request.getCredits());

            log.info("결제 검증 완료 - userId: {}, credits: {}", userId, request.getCredits());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("결제 검증 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}