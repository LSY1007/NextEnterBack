package org.zerock.nextenter.ai.resume;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url:http://localhost:8000/api/v1}")
    private String aiServerUrl;

    public String analyzeResume(String text) {
        String url = aiServerUrl + "/analyze";

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

    public String recommendCompanies(Map<String, Object> resumeData) {
        String url = aiServerUrl + "/recommend";

        try {
            // 명시적으로 JSON 직렬화 및 Content-Type 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ObjectMapper로 JSON 문자열로 변환
            String jsonBody = objectMapper.writeValueAsString(resumeData);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "연동 에러: " + e.getMessage();
        }
    }
}