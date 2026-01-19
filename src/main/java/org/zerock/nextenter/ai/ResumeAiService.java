package org.zerock.nextenter.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    public String analyzeResume(String text) {
        String url = aiServerUrl + "/analyze";
        // Assuming the Python server expects a JSON with key "text"
        Map<String, String> request = Map.of("text", text);
        
        try {
            return restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            return "연동 에러: " + e.getMessage();
        }
    }
}
