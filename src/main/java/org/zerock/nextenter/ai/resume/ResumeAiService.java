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
            Map<String, Object> aiRequestMap = request.toAiFormat();

            // 4. JSON 문자열로 직접 변환 (Pretty Print로 가독성 향상)
            String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(aiRequestMap);

            // [DEBUG] 각 섹션별 데이터 상태 로그
            @SuppressWarnings("unchecked")
            Map<String, Object> debugContent = (Map<String, Object>) aiRequestMap.get("resume_content");
            if (debugContent != null) {
                log.info("📊 [AI 전송 데이터 상세]");
                log.info("  - target_role: {}", aiRequestMap.get("target_role"));

                // 학력
                Object edu = debugContent.get("education");
                log.info("  - education: {} 건", (edu instanceof java.util.List) ? ((java.util.List<?>) edu).size() : "0");

                // 경력
                Object career = debugContent.get("professional_experience");
                log.info("  - professional_experience: {} 건", (career instanceof java.util.List) ? ((java.util.List<?>) career).size() : "0");

                // 프로젝트
                Object proj = debugContent.get("project_experience");
                log.info("  - project_experience: {} 건", (proj instanceof java.util.List) ? ((java.util.List<?>) proj).size() : "0");

                // 스킬
                Object skills = debugContent.get("skills");
                if (skills instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> skillsMap = (Map<String, Object>) skills;
                    Object essential = skillsMap.get("essential");
                    log.info("  - skills (essential): {} 건", (essential instanceof java.util.List) ? ((java.util.List<?>) essential).size() : "0");
                }
            }

            log.info("📦 [AI 전송 데이터 (Pretty)]:\n{}", jsonPayload);

            // raw_text 길이 확인 (디버깅용)
            // 1. 최상위 레벨 raw_text 체크
            String topLevelRawText = (String) aiRequestMap.get("raw_text");
            if (topLevelRawText != null && !topLevelRawText.isEmpty()) {
                log.info("📝 [최상위] raw_text 길이: {} 글자", topLevelRawText.length());
                log.debug("📝 [최상위] raw_text 미리보기: {}", topLevelRawText.substring(0, Math.min(200, topLevelRawText.length())) + "...");
            } else {
                log.warn("⚠️ [최상위] raw_text가 비어있습니다!");
            }
            
            // 2. resume_content 안의 raw_text 체크
            @SuppressWarnings("unchecked")
            Map<String, Object> resumeContent = (Map<String, Object>) aiRequestMap.get("resume_content");
            if (resumeContent != null) {
                String rawText = (String) resumeContent.get("raw_text");
                if (rawText != null && !rawText.isEmpty()) {
                    log.info("📝 [resume_content] raw_text 길이: {} 글자", rawText.length());
                } else {
                    log.warn("⚠️ [resume_content] raw_text가 비어있습니다!");
                }
            }

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

        } catch (org.springframework.web.client.RestClientResponseException
                | org.springframework.web.client.ResourceAccessException e) {
            // Controller에서 구체적인 에러 처리를 위해 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("❌ [통신 에러] {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 서버 연결 실패: " + e.getMessage());
        }
    }
}