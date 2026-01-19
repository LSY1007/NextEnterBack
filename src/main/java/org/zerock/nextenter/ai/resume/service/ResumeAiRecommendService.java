package org.zerock.nextenter.ai.resume.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.ai.resume.ResumeAiService;
import org.zerock.nextenter.ai.resume.dto.AiRecommendRequest;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;
import org.zerock.nextenter.ai.resume.entity.ResumeAiRecommend;
import org.zerock.nextenter.ai.resume.repository.ResumeAiRecommendRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAiRecommendService {

    private final ResumeAiService resumeAiService;
    private final ResumeAiRecommendRepository recommendRepository;
    private final ObjectMapper objectMapper;

    /**
     * AI 추천 요청 + DB 저장
     */
    @Transactional
    public AiRecommendResponse recommendAndSave(AiRecommendRequest request) {
        // 1. AI 서버 호출
        String aiResponseJson = resumeAiService.recommendCompanies(request.toAiRequestMap());
        log.info("AI 서버 응답: {}", aiResponseJson);

        // 2. AI 응답 파싱
        String aiReport = extractAiReport(aiResponseJson);
        List<AiRecommendResponse.CompanyRecommend> companies = extractCompanies(aiResponseJson);

        // 3. DB 저장
        ResumeAiRecommend entity = ResumeAiRecommend.builder()
                .resumeId(request.getResumeId())
                .userId(request.getUserId())
                .aiResponse(aiResponseJson)
                .aiReport(aiReport)
                .build();

        ResumeAiRecommend saved = recommendRepository.save(entity);
        log.info("AI 추천 결과 저장 완료: recommendId={}, resumeId={}", 
                saved.getRecommendId(), saved.getResumeId());

        // 4. Response DTO 반환
        return AiRecommendResponse.builder()
                .recommendId(saved.getRecommendId())
                .resumeId(saved.getResumeId())
                .userId(saved.getUserId())
                .companies(companies)
                .aiReport(aiReport)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * 저장된 추천 이력 조회 (이력서 ID 기준)
     */
    @Transactional(readOnly = true)
    public List<AiRecommendResponse> getHistoryByResumeId(Long resumeId) {
        List<ResumeAiRecommend> histories = recommendRepository.findByResumeIdOrderByCreatedAtDesc(resumeId);
        return histories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 저장된 추천 이력 조회 (사용자 ID 기준)
     */
    @Transactional(readOnly = true)
    public List<AiRecommendResponse> getHistoryByUserId(Long userId) {
        List<ResumeAiRecommend> histories = recommendRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return histories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 추천 결과 조회
     */
    @Transactional(readOnly = true)
    public AiRecommendResponse getRecommendById(Long recommendId) {
        ResumeAiRecommend entity = recommendRepository.findById(recommendId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 추천 결과입니다: " + recommendId));
        return convertToResponse(entity);
    }

    // Entity -> Response DTO 변환
    private AiRecommendResponse convertToResponse(ResumeAiRecommend entity) {
        List<AiRecommendResponse.CompanyRecommend> companies = extractCompanies(entity.getAiResponse());
        
        return AiRecommendResponse.builder()
                .recommendId(entity.getRecommendId())
                .resumeId(entity.getResumeId())
                .userId(entity.getUserId())
                .companies(companies)
                .aiReport(entity.getAiReport())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // AI 응답에서 ai_report 추출
    private String extractAiReport(String aiResponseJson) {
        try {
            Map<String, Object> response = objectMapper.readValue(aiResponseJson, 
                    new TypeReference<Map<String, Object>>() {});
            Object aiReport = response.get("ai_report");
            return aiReport != null ? aiReport.toString() : null;
        } catch (JsonProcessingException e) {
            log.warn("AI 응답 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    // AI 응답에서 companies 추출
    @SuppressWarnings("unchecked")
    private List<AiRecommendResponse.CompanyRecommend> extractCompanies(String aiResponseJson) {
        try {
            Map<String, Object> response = objectMapper.readValue(aiResponseJson, 
                    new TypeReference<Map<String, Object>>() {});
            
            List<Map<String, Object>> companiesList = (List<Map<String, Object>>) response.get("companies");
            if (companiesList == null) {
                return new ArrayList<>();
            }

            return companiesList.stream()
                    .map(company -> AiRecommendResponse.CompanyRecommend.builder()
                            .companyName((String) company.get("company_name"))
                            .role((String) company.get("role"))
                            .score(toDouble(company.get("score")))
                            .matchLevel((String) company.get("match_level"))
                            .isExactMatch((Boolean) company.get("is_exact_match"))
                            .build())
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.warn("AI 응답 companies 파싱 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }
}
