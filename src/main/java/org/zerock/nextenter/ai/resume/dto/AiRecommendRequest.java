package org.zerock.nextenter.ai.resume.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [문제 해결 버전]
 * 이력서 내용을 담을 필드(resumeText)가 추가되었습니다.
 * 이제 빈 껍데기가 아니라 진짜 데이터를 운반합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendRequest {

    private Long resumeId;
    private Long userId;

    // ⭐ [추가됨] 이 필드가 없어서 그동안 내용이 안 갔던 겁니다!
    private String resumeText; 
    
    // 이력서 기본 정보
    private String jobCategory;      // "backend", "ui/ux", "pm" 등
    private List<String> skills;     // ["Java", "Spring"]
    private Integer experience;      // 경력 (년)
    private String education;        // 학력
    private String preferredLocation; 
    
    // 파이썬 서버로 보낼 데이터를 Map으로 예쁘게 포장하는 메서드
    public Map<String, Object> toAiRequestMap() {
        Map<String, Object> result = new HashMap<>();
        
        // 1. ID 정보 (문자열로 변환하여 안전하게 전송)
        result.put("id", resumeId != null ? String.valueOf(resumeId) : "unknown");
        result.put("user_id", userId != null ? String.valueOf(userId) : "unknown");
        
        // 2. 직무 타겟팅
        String targetRole = convertJobCategoryToRole(this.jobCategory);
        result.put("target_role", targetRole);
        
        // 3. ⭐ [핵심] 이력서 내용물 채우기
        // Python 서버가 'resume_content' 혹은 'resume_text' 중 뭘 좋아할지 몰라 둘 다 넣습니다. (안전빵)
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("raw_text", this.resumeText != null ? this.resumeText : "");
        contentMap.put("skills", this.skills != null ? this.skills : new ArrayList<>());
        contentMap.put("experience_years", this.experience != null ? this.experience : 0);
        contentMap.put("education", this.education != null ? this.education : "");
        
        // Python 구조에 맞춰서 데이터 삽입
        result.put("resume_content", contentMap);
        result.put("resume_text", this.resumeText != null ? this.resumeText : ""); // 혹시 몰라 최상위에도 넣음
        
        return result;
    }
    
    // 직무명 표준화 로직
    private String convertJobCategoryToRole(String category) {
        if (category == null) return "Backend Developer";
        
        String lower = category.toLowerCase().trim();
        
        if (lower.contains("ui") || lower.contains("ux") || lower.contains("design") || lower.contains("디자인")) {
            return "UI/UX Designer";
        }
        if (lower.contains("pm") || lower.contains("기획") || lower.contains("manager")) {
            return "Product Manager";
        }
        if (lower.contains("front") || lower.contains("프론트")) return "Frontend Developer";
        if (lower.contains("full") || lower.contains("풀스택")) return "Fullstack Developer";
        
        return "Backend Developer"; // 기본값
    }
}