package org.zerock.nextenter.ai.resume;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.ai.resume.dto.AiRecommendRequest;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;
import org.zerock.nextenter.ai.resume.service.ResumeAiRecommendService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/resume")
@RequiredArgsConstructor
public class ResumeAiController {

    private final ResumeAiService resumeAiService;
    private final ResumeAiRecommendService resumeAiRecommendService;

    @GetMapping("/test")
    public String testAnalyze(@RequestParam String text) {
        return resumeAiService.analyzeResume(text);
    }

    // 기존: DB 저장 없이 AI 결과만 반환
    @PostMapping("/recommend")
    public String testRecommend(@RequestBody Map<String, Object> resumeData) {
        return resumeAiService.recommendCompanies(resumeData);
    }

    // 신규: AI 추천 + DB 저장
    @PostMapping("/recommend/save")
    public AiRecommendResponse recommendAndSave(@RequestBody AiRecommendRequest request) {
        return resumeAiRecommendService.recommendAndSave(request);
    }

    // 신규: 저장된 추천 이력 조회 (이력서 ID 기준)
    @GetMapping("/recommend/history/resume/{resumeId}")
    public List<AiRecommendResponse> getHistoryByResume(@PathVariable Long resumeId) {
        return resumeAiRecommendService.getHistoryByResumeId(resumeId);
    }

    // 신규: 저장된 추천 이력 조회 (사용자 ID 기준)
    @GetMapping("/recommend/history/user/{userId}")
    public List<AiRecommendResponse> getHistoryByUser(@PathVariable Long userId) {
        return resumeAiRecommendService.getHistoryByUserId(userId);
    }

    // 신규: 특정 추천 결과 조회
    @GetMapping("/recommend/{recommendId}")
    public AiRecommendResponse getRecommend(@PathVariable Long recommendId) {
        return resumeAiRecommendService.getRecommendById(recommendId);
    }
}
