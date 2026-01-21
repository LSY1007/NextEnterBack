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
import org.springframework.web.client.RestTemplate;
import org.zerock.nextenter.ai.resume.dto.AiRecommendRequest;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * [최종 해결 + 방어 로직 강화 버전]
 * 1. 데이터 검증(Validation)을 추가하여 빈 데이터 전송을 원천 차단합니다.
 * 2. 순정 RestTemplate을 사용하여 설정 꼬임 방지 및 이중 인코딩을 해결합니다.
 * 3. 상세한 로깅으로 전송되는 데이터를 투명하게 확인합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAiService {

    // Global 설정(RestTemplateConfig)은 간섭을 피하기 위해 사용하지 않음
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url:http://localhost:8000/api/v1}")
    private String aiServerUrl;

    public AiRecommendResponse fetchRecommendation(AiRecommendRequest request) {
        String url = aiServerUrl + "/analyze";
        log.info("🚀 [AI] 분석 요청 시작! URL: {}", url);

        // 1. 🛡️ [방어 로직] 출발 전 데이터 검증 (여기서 걸리면 바로 중단)
        validateRequest(request);
        try {
            // ✅ recommendCompanies와 동일한 방식으로 수정 (JSON 형식 명시)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 파이썬 서버가 요구하는 필드명 "resume_text"로 객체 생성
            Map<String, String> requestBody = Map.of("resume_text", text);
            
            // ObjectMapper로 JSON 문자열로 변환
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "연동 에러: " + e.getMessage();
        }
    }

        try {
            // 2. 헤더 설정 (JSON + UTF-8 명시)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            // 3. 데이터 준비 (DTO -> Map)
            Map<String, Object> aiRequestMap = request.toAiRequestMap();
            
            // 4. Map 검증 (변환 과정에서 비어버렸는지 확인)
            if (aiRequestMap == null || aiRequestMap.isEmpty()) {
                throw new IllegalArgumentException("❌ 변환된 요청 데이터(Map)가 비어있습니다. DTO 변환 로직을 확인하세요.");
            }

            // 5. JSON 문자열 변환 (전송될 실제 형태)
            String jsonPayload = objectMapper.writeValueAsString(aiRequestMap);
            
            // 🔍 [CCTV] 전송될 데이터 낱낱이 확인
            log.debug("📦 [AI 전송 데이터] Payload Length: {}", jsonPayload.length());
            log.info("📦 [AI 전송 데이터] Body: {}", jsonPayload);

            // 6. HttpEntity 포장
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            // 7. 🚀 [핵심] '순정' RestTemplate 즉석 생성
            // Global 설정의 간섭을 100% 차단하고 문자열 그대로를 보냅니다.
            RestTemplate directRestTemplate = new RestTemplate();
            directRestTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            // 8. 전송
            ResponseEntity<String> responseEntity = directRestTemplate.postForEntity(url, requestEntity, String.class);

            // 9. 응답 확인
            log.info("✅ [AI] 응답 도착! 상태코드: {}", responseEntity.getStatusCode());
            String rawResponse = responseEntity.getBody();

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new RuntimeException("Python 서버로부터 빈 응답이 도착했습니다.");
            }

            // 10. 결과 변환
            return objectMapper.readValue(rawResponse, AiRecommendResponse.class);

        } catch (IllegalArgumentException e) {
            // 데이터 검증 실패는 즉시 로그 남기고 던짐
            log.error("🛑 [데이터 검증 실패] {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ [AI 통신 에러] 상세 내용: ", e); // 스택 트레이스 전체 출력
            throw new RuntimeException("AI 서버 통신 실패: " + e.getMessage());
        }
    }

    /**
     * 요청 데이터가 유효한지 검사하는 내부 메서드
     */
    private void validateRequest(AiRecommendRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 객체(AiRecommendRequest) 자체가 null입니다.");
        }
        if (request.getUserId() == null) {
            log.warn("⚠️ 경고: userId가 없습니다. (로깅 식별 불가)");
        }
        // 가장 중요한 '이력서 내용' 체크
        if (request.getResumeText() == null || request.getResumeText().trim().isEmpty()) {
            throw new IllegalArgumentException("❌ 핵심 데이터인 'resumeText'가 비어있습니다! 프론트엔드 전송 값을 확인하세요.");
        }
    }
}