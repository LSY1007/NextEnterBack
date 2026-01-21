package org.zerock.nextenter.ai.resume.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAiRecommendService {

    private final ResumeAiService resumeAiService; 
    private final ResumeAiRecommendRepository recommendRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AiRecommendResponse recommendAndSave(AiRecommendRequest request) {
        log.info("🚀 AI 추천 및 저장 프로세스 시작 (userId: {})", request.getUserId());

        // 1. 비서에게 시켜서 파이썬 서버 데이터 가져오기 (422 해결된 메서드 호출)
        AiRecommendResponse responseDto = resumeAiService.fetchRecommendation(request);
        
        // 2. 응답에 유저 정보 보강
        responseDto.setUserId(request.getUserId());

        // 3. DB 저장
        try {
            saveToDatabase(request, responseDto);
        } catch (Exception e) {
            log.error("⚠️ [DB Error] 저장 실패: {}", e.getMessage());
        }

        return responseDto;
    }

    private void saveToDatabase(AiRecommendRequest request, AiRecommendResponse responseDto) throws JsonProcessingException {
        String fullJson = objectMapper.writeValueAsString(responseDto);

        ResumeAiRecommend entity = ResumeAiRecommend.builder()
                .resumeId(request.getResumeId())
                .userId(request.getUserId())
                .aiResponse(fullJson)
                .aiReport(responseDto.getAiReport())
                .build();

        ResumeAiRecommend saved = recommendRepository.save(entity);
        
        responseDto.setRecommendId(saved.getRecommendId());
        responseDto.setCreatedAt(saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<AiRecommendResponse> getHistoryByUserId(Long userId) {
        List<ResumeAiRecommend> histories = recommendRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return histories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private AiRecommendResponse convertToResponse(ResumeAiRecommend entity) {
        try {
            AiRecommendResponse dto = objectMapper.readValue(entity.getAiResponse(), AiRecommendResponse.class);
            dto.setRecommendId(entity.getRecommendId());
            dto.setResumeId(entity.getResumeId() != null ? String.valueOf(entity.getResumeId()) : null);
            dto.setUserId(entity.getUserId());
            dto.setCreatedAt(entity.getCreatedAt());
            return dto;
        } catch (Exception e) {
            log.error("데이터 복구 실패: {}", e.getMessage());
            return null;
        }
    }
}