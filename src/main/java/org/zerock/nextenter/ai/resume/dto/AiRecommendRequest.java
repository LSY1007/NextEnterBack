package org.zerock.nextenter.ai.resume.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [최종 해결 버전]
 * 1. 학력 정보(String)를 본문(resumeText)에 합쳐서 AI에게 전달 (누락 방지)
 * 2. 파이썬 서버의 타입 에러를 막기 위해 education 필드엔 빈 리스트([]) 전송
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendRequest {

    private Long resumeId;
    private Long userId;

    private String resumeText; 
    
    // 이력서 기본 정보
    private String jobCategory;
    private List<String> skills;
    private Integer experience;
    private String education;        // 여기에 "2020.03 ~ ..." 문자열이 들어옴
    private String preferredLocation; 
    
    public Map<String, Object> toAiRequestMap() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("id", resumeId != null ? String.valueOf(resumeId) : "unknown");
        result.put("user_id", userId != null ? String.valueOf(userId) : "unknown");
        result.put("target_role", convertJobCategoryToRole(this.jobCategory));
        
        // 1. [핵심] 학력 정보가 있다면 본문 뒤에 붙여넣기 (AI가 읽을 수 있도록)
        StringBuilder fullTextBuilder = new StringBuilder();
        if (this.resumeText != null) fullTextBuilder.append(this.resumeText).append("\n");
        
        if (this.education != null && !this.education.isEmpty()) {
            fullTextBuilder.append("\n[학력 정보]\n").append(this.education);
        }
        
        String combinedText = fullTextBuilder.toString();

        // 2. 내용물 채우기
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("raw_text", combinedText); // 합쳐진 전체 텍스트
        contentMap.put("skills", this.skills != null ? this.skills : new ArrayList<>());
        contentMap.put("experience_years", this.experience != null ? this.experience : 0);
        
        // 3. [속임수] 파이썬 에러 방지용 빈 리스트
        // (실제 정보는 위 raw_text에 다 들어있음)
        contentMap.put("education", new ArrayList<>()); 
        
        result.put("resume_content", contentMap);
        result.put("resume_text", combinedText);
        
        return result;
    }
    
    // 직무명 변환기
    private String convertJobCategoryToRole(String category) {
        if (category == null) return "Backend Developer";
        String lower = category.toLowerCase().trim();
        if (lower.contains("ui") || lower.contains("ux") || lower.contains("design")) return "UI/UX Designer";
        if (lower.contains("pm") || lower.contains("기획")) return "Product Manager";
        if (lower.contains("front")) return "프론트엔드";
        if (lower.contains("full")) return "Fullstack Developer";
        return "Backend Developer";
    }
}