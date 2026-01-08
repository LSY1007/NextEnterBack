package org.zerock.nextenter.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.recommendation.dto.RecommendationDto;
import org.zerock.nextenter.recommendation.dto.RecommendationHistoryDto;
import org.zerock.nextenter.recommendation.dto.RecommendationRequest;
import org.zerock.nextenter.recommendation.service.RecommendationService;

@Tag(name = "Recommendation", description = "AI 공고 추천 API")
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "AI 공고 추천", description = "이력서를 기반으로 TOP 5 공고를 추천받습니다 (크레딧 50 사용)")
    @PostMapping
    public ResponseEntity<RecommendationDto> getRecommendation(
            @RequestParam(required = false) Long userId,  // 임시
            @Valid @RequestBody RecommendationRequest request) {
        if (userId == null) userId = 1L;
        RecommendationDto result = recommendationService.getRecommendation(userId, request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "추천 히스토리", description = "과거 추천 받은 내역을 조회합니다")
    @GetMapping("/history")
    public ResponseEntity<Page<RecommendationHistoryDto>> getHistory(
            @RequestParam(required = false) Long userId,  // 임시
            Pageable pageable) {
        if (userId == null) userId = 1L;
        Page<RecommendationHistoryDto> result = recommendationService.getHistory(userId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "추천 결과 상세", description = "특정 추천 결과의 상세 정보를 조회합니다")
    @GetMapping("/{recommendationId}")
    public ResponseEntity<RecommendationDto> getDetail(
            @RequestParam(required = false) Long userId,  // 임시
            @PathVariable Long recommendationId) {
        if (userId == null) userId = 1L;
        RecommendationDto result = recommendationService.getDetail(userId, recommendationId);
        return ResponseEntity.ok(result);
    }
}