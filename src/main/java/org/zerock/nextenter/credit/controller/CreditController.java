package org.zerock.nextenter.credit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.credit.dto.CreditBalanceDto;
import org.zerock.nextenter.credit.dto.CreditChargeRequest;
import org.zerock.nextenter.credit.service.CreditService;

import java.util.HashMap;
import java.util.Map;

/**
 * 크레딧 관리 API 컨트롤러
 */
@Tag(name = "Credit", description = "크레딧 관리 API")
@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
@Slf4j
public class CreditController {

    private final CreditService creditService;

    // ==================== 크레딧 조회 ====================

    @Operation(summary = "크레딧 잔액 조회", description = "현재 사용자의 크레딧 잔액을 조회합니다")
    @GetMapping("/balance")
    public ResponseEntity<CreditBalanceDto> getBalance(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/credit/balance - userId: {}", userId);

        CreditBalanceDto balance = creditService.getBalance(userId);

        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "크레딧 충분 여부 확인", description = "특정 금액의 크레딧이 충분한지 확인합니다")
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkCredit(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId,
            @Parameter(description = "필요 금액", required = true)
            @RequestParam int amount
    ) {
        log.info("GET /api/credit/check - userId: {}, amount: {}", userId, amount);

        boolean hasEnough = creditService.hasEnoughCredit(userId, amount);

        Map<String, Object> response = new HashMap<>();
        response.put("hasEnoughCredit", hasEnough);
        response.put("requiredAmount", amount);

        return ResponseEntity.ok(response);
    }

    // ==================== 크레딧 충전 ====================

    @Operation(summary = "크레딧 충전", description = "크레딧을 충전합니다")
    @PostMapping("/charge")
    public ResponseEntity<Map<String, Object>> chargeCredit(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId,
            @Parameter(description = "충전 요청 정보", required = true)
            @RequestBody @Valid CreditChargeRequest request
    ) {
        log.info("POST /api/credit/charge - userId: {}, amount: {}", userId, request.getAmount());

        try {
            CreditBalanceDto balance = creditService.charge(
                    userId,
                    request.getAmount(),
                    request.getDescription()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "크레딧이 충전되었습니다");
            response.put("balance", balance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("크레딧 충전 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== 크레딧 차감 ====================

    @Operation(summary = "크레딧 차감", description = "매칭/추천/면접 등에서 크레딧을 차감합니다")
    @PostMapping("/deduct")
    public ResponseEntity<Map<String, Object>> deductCredit(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId,
            @Parameter(description = "차감 금액", required = true)
            @RequestParam int amount,
            @Parameter(description = "차감 사유")
            @RequestParam(required = false) String description
    ) {
        log.info("POST /api/credit/deduct - userId: {}, amount: {}", userId, amount);

        try {
            CreditBalanceDto balance = creditService.deduct(userId, amount, description);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "크레딧이 차감되었습니다");
            response.put("balance", balance);

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // 잔액 부족
            log.warn("크레딧 부족 - userId: {}, amount: {}", userId, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("크레딧 차감 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "크레딧 차감 중 오류가 발생했습니다");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 테스트용 ====================

    @Operation(summary = "크레딧 초기화 (테스트용)", description = "크레딧을 0으로 초기화합니다")
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetCredit(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("POST /api/credit/reset - userId: {}", userId);

        creditService.resetCredit(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "크레딧이 초기화되었습니다");

        return ResponseEntity.ok(response);
    }
}