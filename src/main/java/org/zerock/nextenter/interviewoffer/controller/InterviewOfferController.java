package org.zerock.nextenter.interviewoffer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.interviewoffer.dto.InterviewOfferRequest;
import org.zerock.nextenter.interviewoffer.dto.InterviewOfferResponse;
import org.zerock.nextenter.interviewoffer.service.InterviewOfferService;

import java.util.List;

@RestController
@RequestMapping("/api/interview-offers")
@RequiredArgsConstructor
public class InterviewOfferController {

    private final InterviewOfferService interviewOfferService;

    // 1. 기업이 면접 제안 생성
    @PostMapping
    public ResponseEntity<InterviewOfferResponse> createOffer(
            @RequestHeader("companyId") Long companyId,
            @RequestBody InterviewOfferRequest request
    ) {
        return ResponseEntity.ok(interviewOfferService.createOffer(companyId, request));
    }

    // 2. 사용자가 받은 면접 제안 목록 (OFFERED 상태만)
    @GetMapping("/received")
    public ResponseEntity<List<InterviewOfferResponse>> getReceivedOffers(@RequestHeader("userId") Long userId) {
        return ResponseEntity.ok(interviewOfferService.getReceivedOffers(userId));
    }

    // 3. 사용자의 모든 면접 제안 조회 (deleted 필터 포함)
    @GetMapping("/my")
    public ResponseEntity<List<InterviewOfferResponse>> getMyOffers(
            @RequestHeader("userId") Long userId,
            @RequestParam(required = false) Boolean includeDeleted
    ) {
        return ResponseEntity.ok(interviewOfferService.getMyOffers(userId, includeDeleted));
    }

    // 4. 기업의 면접 제안 목록
    @GetMapping("/company")
    public ResponseEntity<List<InterviewOfferResponse>> getCompanyOffers(
            @RequestHeader("companyId") Long companyId,
            @RequestParam(required = false) Long jobId
    ) {
        return ResponseEntity.ok(interviewOfferService.getCompanyOffers(companyId, jobId));
    }

    // 5. 면접 제안 수락
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<InterviewOfferResponse> acceptOffer(
            @PathVariable Long offerId,
            @RequestHeader("userId") Long userId
    ) {
        return ResponseEntity.ok(interviewOfferService.acceptOffer(offerId, userId));
    }

    // 6. 면접 제안 거절
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<InterviewOfferResponse> rejectOffer(
            @PathVariable Long offerId,
            @RequestHeader("userId") Long userId
    ) {
        return ResponseEntity.ok(interviewOfferService.rejectOffer(offerId, userId));
    }

    // 7. 면접 제안 단일 삭제
    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> deleteOffer(
            @PathVariable Long offerId,
            @RequestHeader("userId") Long userId
    ) {
        interviewOfferService.deleteOffer(offerId, userId);
        return ResponseEntity.ok().build();
    }

    // ✅ [추가됨] 면접 제안 일괄 삭제
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteOffers(
            @RequestBody List<Long> offerIds,
            @RequestHeader("userId") Long userId
    ) {
        interviewOfferService.deleteOffers(offerIds, userId);
        return ResponseEntity.ok().build();
    }

    // 8. 제안한 공고 ID 목록
    @GetMapping("/company/offered-jobs")
    public ResponseEntity<List<Long>> getOfferedJobIds(
            @RequestHeader("companyId") Long companyId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(interviewOfferService.getOfferedJobIds(companyId, userId));
    }
}