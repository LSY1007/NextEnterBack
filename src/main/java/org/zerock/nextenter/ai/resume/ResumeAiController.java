package org.zerock.nextenter.ai.resume;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.ai.resume.dto.AiRecommendRequest;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;
import org.zerock.nextenter.ai.resume.service.ResumeAiRecommendService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ResumeAiController {

    private final ResumeAiRecommendService resumeAiRecommendService;

    /**
     * AI 추천 요청 및 결과 저장
     */
    @PostMapping("/recommend")
    public ResponseEntity<AiRecommendResponse> recommendAndSave(@RequestBody AiRecommendRequest request) {
        log.info("📥 [Controller] AI 추천 요청 수신: resumeId={}, userId={}", request.getResumeId(), request.getUserId());
        
        try {
            AiRecommendResponse response = resumeAiRecommendService.recommendAndSave(request);
            log.info("✅ [Controller] 요청 처리 완료: recommendId={}", response.getRecommendId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [Controller] 분석 실패: {}", e.getMessage());
            // 상세 에러 메시지를 포함하여 예외를 던집니다.
            throw new RuntimeException("AI 분석 요청 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자별 추천 이력 조회
     */
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<AiRecommendResponse>> getHistoryByUser(@PathVariable Long userId) {
        log.info("📥 [Controller] 히스토리 조회 요청: userId={}", userId);
        return ResponseEntity.ok(resumeAiRecommendService.getHistoryByUserId(userId));
    }
}