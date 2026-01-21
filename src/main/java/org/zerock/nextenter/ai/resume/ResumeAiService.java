package org.zerock.nextenter.ai.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.zerock.nextenter.ai.resume.dto.AiRecommendRequest;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * [최종 해결 버전]
 * 1. 빈 데이터 전송 방지
 * 2. '순정' RestTemplate 사용으로 설정 충돌 및 이중 인코딩 해결
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final ObjectMapper objectMapper;

    @Value("${ai.server.url:http://localhost:8000/api/v1}")
    private String aiServerUrl;

    public AiRecommendResponse fetchRecommendation(AiRecommendRequest request) {
        String url = aiServerUrl + "/analyze";
        log.info("🚀 [AI] 요청 시작! URL: {}", url);

        // 1. 데이터 검증
        if (request == null || request.getResumeText() == null) {
             throw new IllegalArgumentException("❌ 이력서 내용(resumeText)이 비어있습니다.");
        }

        try {
            // 2. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            // 3. 데이터 준비 (위에서 수정한 DTO 로직 사용)
            Map<String, Object> aiRequestMap = request.toAiRequestMap();
            
            // 4. JSON 문자열로 직접 변환 (눈으로 확인 가능)
            String jsonPayload = objectMapper.writeValueAsString(aiRequestMap);
            log.info("📦 [AI 전송 데이터]: {}", jsonPayload);

            // 5. HttpEntity 포장
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            // 6. '순정' RestTemplate 생성 (설정 꼬임 방지)
            RestTemplate directRestTemplate = new RestTemplate();
            directRestTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            // 7. 전송
            ResponseEntity<String> responseEntity = directRestTemplate.postForEntity(url, requestEntity, String.class);

            // 8. 응답 처리
            log.info("✅ [AI] 응답 성공! 상태: {}", responseEntity.getStatusCode());
            String rawResponse = responseEntity.getBody();

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new RuntimeException("AI 서버로부터 빈 응답이 왔습니다.");
            }

            return objectMapper.readValue(rawResponse, AiRecommendResponse.class);

        } catch (RestClientResponseException e) {
            log.error("❌ [AI 서버 에러] 상태: {}, 내용: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 분석 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("❌ [통신 에러] {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 서버 연결 실패: " + e.getMessage());
        }
    }
}