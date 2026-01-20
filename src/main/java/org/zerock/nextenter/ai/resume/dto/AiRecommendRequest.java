package org.zerock.nextenter.ai.resume.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendRequest {

    private Long resumeId;
    private Long userId;
    
    // AI 서버로 전달할 이력서 데이터
    private String jobCategory;      // 직무 카테고리 (backend, frontend 등)
    private List<String> skills;     // 보유 스킬 리스트
    private Integer experience;      // 경력 년수
    private String education;        // 학력
    private String preferredLocation; // 선호 지역
    
    // AI 서버 요청 형식으로 변환 (NextEnterAI ResumeInputDTO 형식에 맞춤)
    public Map<String, Object> toAiRequestMap() {
        // ID 생성 (resumeId 기반)
        String id = "RESUME_" + (resumeId != null ? resumeId : "UNKNOWN");
        
        // target_role 설정 (jobCategory를 직무명으로 변환)
        String targetRole = convertJobCategoryToRole(jobCategory);
        
        // education 리스트 생성
        List<Map<String, String>> educationList = List.of(
            Map.of(
                "degree", education != null ? education : "미기재",
                "major", "관련학과",
                "status", "졸업"
            )
        );
        
        // skills 맵 생성
        Map<String, List<String>> skillsMap = Map.of(
            "essential", skills != null ? skills : List.of(),
            "additional", List.of()
        );
        
        // professional_experience 리스트 생성
        List<Map<String, Object>> experienceList = List.of(
            Map.of(
                "company", "이전회사",
                "period", (experience != null ? experience * 12 : 0) + "개월",
                "role", targetRole,
                "key_tasks", List.of("업무 수행")
            )
        );
        
        // resume_content 구조 생성
        Map<String, Object> resumeContent = Map.of(
            "education", educationList,
            "skills", skillsMap,
            "professional_experience", experienceList,
            "project_experience", List.of()
        );
        
        // 최종 요청 맵 생성
        return Map.of(
            "id", id,
            "target_role", targetRole,
            "resume_content", resumeContent
        );
    }
    
    // 직무 카테고리를 구체적인 직무명으로 변환
    private String convertJobCategoryToRole(String category) {
        if (category == null) return "Developer";
        
        String lowerCategory = category.toLowerCase();
        
        // 한글 직무명 처리
        if (lowerCategory.contains("백엔드") || lowerCategory.equals("backend")) {
            return "Backend Developer";
        } else if (lowerCategory.contains("프론트엔드") || lowerCategory.equals("frontend")) {
            return "Frontend Developer";
        } else if (lowerCategory.contains("풀스택") || lowerCategory.equals("fullstack")) {
            return "Fullstack Developer";
        } else if (lowerCategory.contains("ai") || lowerCategory.contains("인공지능")) {
            return "AI Engineer";
        } else if (lowerCategory.contains("데이터") || lowerCategory.equals("data")) {
            return "Data Scientist";
        } else if (lowerCategory.contains("모바일") || lowerCategory.equals("mobile")) {
            return "Mobile Developer";
        } else if (lowerCategory.contains("데브옵스") || lowerCategory.equals("devops")) {
            return "DevOps Engineer";
        }
        
        // 영문 처리 (fallback)
        switch (lowerCategory) {
            case "backend": return "Backend Developer";
            case "frontend": return "Frontend Developer";
            case "fullstack": return "Fullstack Developer";
            case "ai": return "AI Engineer";
            case "data": return "Data Scientist";
            case "mobile": return "Mobile Developer";
            case "devops": return "DevOps Engineer";
            default: return category + " Developer";
        }
    }
}
