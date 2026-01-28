package org.zerock.nextenter.interviewoffer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.interviewoffer.dto.InterviewOfferRequest;
import org.zerock.nextenter.interviewoffer.dto.InterviewOfferResponse;
import org.zerock.nextenter.interviewoffer.service.InterviewOfferService;

import java.util.List;

@Tag(name = "Interview Offer", description = "면접 제안 관리 API")
@RestController
@RequestMapping("/api/interview-offers")
@RequiredArgsConstructor
@Slf4j
public class InterviewOfferController {

    private final InterviewOfferService interviewOfferService;

    @Operation(summary = "면접 제안 생성", description = "기업이 인재검색에서 면접을 제안합니다")
    @PostMapping
    public ResponseEntity<InterviewOfferResponse> createOffer(
            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "면접 제안 요청 데이터", required = true)
            @RequestBody InterviewOfferRequest request
    ) {
        log.info("POST /api/interview-offers - companyId: {}, userId: {}, jobId: {}",
                companyId, request.getUserId(), request.getJobId());

        InterviewOfferResponse offer = interviewOfferService.createOffer(companyId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(offer);
    }

    @Operation(summary = "받은 제안 목록 조회", description = "사용자가 받은 면접 제안 목록 (OFFERED 상태만)")
    @GetMapping("/received")
    public ResponseEntity<List<InterviewOfferResponse>> getReceivedOffers(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/interview-offers/received - userId: {}", userId);

        List<InterviewOfferResponse> offers = interviewOfferService.getReceivedOffers(userId);

        return ResponseEntity.ok(offers);
    }

    @Operation(summary = "내 모든 면접 제안 조회", description = "사용자의 모든 면접 제안 내역")
    @GetMapping("/my")
    public ResponseEntity<List<InterviewOfferResponse>> getMyOffers(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/interview-offers/my - userId: {}", userId);

        List<InterviewOfferResponse> offers = interviewOfferService.getMyOffers(userId);

        return ResponseEntity.ok(offers);
    }

    @Operation(summary = "기업의 면접 제안 조회", description = "기업이 보낸 면접 제안 목록")
    @GetMapping("/company")
    public ResponseEntity<List<InterviewOfferResponse>> getCompanyOffers(
            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "공고 ID (특정 공고만 조회)")
            @RequestParam(required = false) Long jobId
    ) {
        log.info("GET /api/interview-offers/company - companyId: {}, jobId: {}", companyId, jobId);

        List<InterviewOfferResponse> offers = interviewOfferService.getCompanyOffers(companyId, jobId);

        return ResponseEntity.ok(offers);
    }

    @Operation(summary = "면접 제안 수락", description = "사용자가 받은 면접 제안을 수락합니다")
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<InterviewOfferResponse> acceptOffer(
            @Parameter(description = "제안 ID", required = true)
            @PathVariable Long offerId,

            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("POST /api/interview-offers/{}/accept - userId: {}", offerId, userId);

        InterviewOfferResponse offer = interviewOfferService.acceptOffer(offerId, userId);

        return ResponseEntity.ok(offer);
    }

    @Operation(summary = "면접 제안 거절", description = "사용자가 받은 면접 제안을 거절합니다")
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<InterviewOfferResponse> rejectOffer(
            @Parameter(description = "제안 ID", required = true)
            @PathVariable Long offerId,

            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("POST /api/interview-offers/{}/reject - userId: {}", offerId, userId);

        InterviewOfferResponse offer = interviewOfferService.rejectOffer(offerId, userId);

        return ResponseEntity.ok(offer);
    }
}
