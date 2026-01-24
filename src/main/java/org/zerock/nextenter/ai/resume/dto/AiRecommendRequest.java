package org.zerock.nextenter.ai.resume.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [최종_정제_완성본_V6]
 * 1. 데이터 중복 제거: 모든 필드에 같은 값을 넣는 대신, 핵심 필드에만 값을 넣고 나머지는 '본문 참조'로 처리
 * 2. 가독성 향상: 파이썬 로그 및 데이터 구조가 훨씬 깔끔해짐
 * 3. Object 타입 유지: 프론트엔드의 데이터 형태(문자열/리스트/객체) 변화에 유연하게 대응
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRecommendRequest {

    private Long resumeId;
    private Long userId;

    @JsonAlias({"content"}) 
    private Object resumeText; 
    
    private String jobCategory;
    
    // [스킬]
    @Builder.Default
    @JsonAlias({"skill", "techStack", "skills"})
    private Object skills = new ArrayList<>(); 
    
    // [경력 기간]
    private Integer experience;       // 년
    private Integer experienceMonths; // 월
    
    // [학력]
    @Builder.Default
    @JsonAlias({"education", "school", "educations"}) 
    private Object educations = new ArrayList<>();

    // [경력]
    @Builder.Default
    @JsonAlias({"career", "careers", "professional_experience", "professional_experiences", "work_experience"})
    private Object careers = new ArrayList<>();

    // [프로젝트]
    @Builder.Default
    @JsonAlias({"project", "projects", "activities", "experiences", "project_experience", "project_experiences"}) 
    private Object projects = new ArrayList<>();
    
    private String preferredLocation; 
    
    // ---------------------------------------------------------
    // 🚀 데이터 변환 로직
    // ---------------------------------------------------------
    public Map<String, Object> toAiRequestMap() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("id", resumeId != null ? String.valueOf(resumeId) : "unknown");
        result.put("target_role", convertJobCategoryToRole(this.jobCategory));
        
        // 데이터 정제 (String List로 변환)
        List<String> cleanEducations = extractTextList(this.educations);
        List<String> cleanCareers = extractTextList(this.careers);
        List<String> cleanProjects = extractTextList(this.projects);
        List<String> cleanSkills = extractTextList(this.skills);

        // 1. [raw_text 통합] AI가 읽을 전체 텍스트 생성
        StringBuilder fullTextBuilder = new StringBuilder();
        String extractedResumeBody = extractString(this.resumeText);
        if (extractedResumeBody != null && !extractedResumeBody.isEmpty()) {
            fullTextBuilder.append(extractedResumeBody);
        }
        
        // 총 경력 기간 텍스트화
        int years = (this.experience != null) ? this.experience : 0;
        int months = (this.experienceMonths != null) ? this.experienceMonths : 0;
        if (years > 0 || months > 0) {
            fullTextBuilder.append("\n\n[총 경력] ").append(years).append("년 ").append(months).append("개월");
        }
        
        appendSection(fullTextBuilder, "[경력 사항]", cleanCareers);
        appendSection(fullTextBuilder, "[프로젝트 및 경험]", cleanProjects);
        appendSection(fullTextBuilder, "[학력 사항]", cleanEducations);
        if (!cleanSkills.isEmpty()) {
            fullTextBuilder.append("\n\n[보유 기술]\n").append(String.join(", ", cleanSkills));
        }

        String finalRawText = fullTextBuilder.toString();

        // 2. resume_content 구성
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("raw_text", finalRawText); 
        contentMap.put("skills", cleanSkills);
        
        double totalYears = years + (months / 12.0);
        contentMap.put("experience_years", Math.round(totalYears * 10) / 10.0);
        
        // (1) 학력 구조화 (깔끔하게 정리)
        List<Map<String, String>> pythonEdu = new ArrayList<>();
        for (String edu : cleanEducations) {
            Map<String, String> map = new HashMap<>();
            map.put("school_name", edu); 
            map.put("major", edu); // 파이썬이 major를 볼 수 있으므로 여기에도 값을 넣어줌
            map.put("degree", "학사"); // 기본값 설정 (없으면 null보단 나음)
            map.put("status", "졸업");
            pythonEdu.add(map);
        }
        contentMap.put("education", pythonEdu);
        
        // (2) 경력 구조화
        List<Map<String, String>> pythonCareer = new ArrayList<>();
        for (String career : cleanCareers) {
            Map<String, String> map = new HashMap<>();
            map.put("role", "Backend Developer"); // 기본값 혹은 파싱된 값
            map.put("company_name", career);
            map.put("period", "3년"); // 기본값 (raw_text 참조 유도)
            map.put("description", career);
            // ⭐ 파이썬 요구사항: key_tasks는 리스트여야 함
            map.put("key_tasks", Collections.singletonList(career)); 
            pythonCareer.add(map);
        }
        contentMap.put("professional_experience", pythonCareer);
        
        // (3) 프로젝트 구조화
        List<Map<String, String>> pythonProject = new ArrayList<>();
        for (String p : cleanProjects) {
            Map<String, String> map = new HashMap<>();
            map.put("project_title", p);
            map.put("description", p);
            pythonProject.add(map);
        }
        contentMap.put("project_experience", pythonProject);

        result.put("resume_content", contentMap);
        
        return result;
    }
    
    // 🛠️ [Object -> String] 추출 헬퍼
    private String extractString(Object input) {
        if (input == null) return null;
        if (input instanceof String) return (String) input;
        if (input instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) input;
            if (map.containsKey("content")) return String.valueOf(map.get("content"));
            List<String> values = new ArrayList<>();
            for (Object val : map.values()) {
                if (val != null) values.add(val.toString());
            }
            return String.join("\n", values);
        }
        return input.toString();
    }

    // 🛠️ [Object -> List<String>] 만능 리스트 추출기
    private List<String> extractTextList(Object input) {
        List<String> result = new ArrayList<>();
        if (input == null) return result;

        if (input instanceof Iterable) {
            for (Object item : (Iterable<?>) input) {
                processSingleItem(item, result);
            }
        } else {
            processSingleItem(input, result);
        }
        return result;
    }

    private void processSingleItem(Object item, List<String> result) {
        if (item == null) return;

        if (item instanceof String) {
            String s = ((String) item).trim();
            if (!s.isEmpty()) result.add(s);
        } else if (item instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) item;
            List<String> values = new ArrayList<>();
            // 가능한 모든 키워드 탐색
            String[] keysToCheck = {
                "company", "companyName", "company_name", 
                "school", "schoolName", "school_name", 
                "project", "projectName", "project_title", 
                "title", "name", "value", "role", "position", 
                "period", "date", "description", "desc", "career", "careers"
            };
            
            for (String key : keysToCheck) {
                Object val = map.get(key);
                if (val != null && !val.toString().trim().isEmpty()) {
                    values.add(val.toString().trim());
                }
            }
            
            if (values.isEmpty()) {
                for (Object val : map.values()) {
                    if (val != null && !val.toString().trim().isEmpty()) {
                        values.add(val.toString().trim());
                    }
                }
            }
            
            if (!values.isEmpty()) {
                result.add(String.join(" | ", values));
            }
        }
    }

    private void appendSection(StringBuilder builder, String title, List<String> items) {
        if (!items.isEmpty()) {
            builder.append("\n\n").append(title).append("\n");
            for (String item : items) {
                builder.append("- ").append(item).append("\n");
            }
        }
    }
    
    private String convertJobCategoryToRole(String category) {
        if (category == null) return "Backend Developer";
        String lower = category.toLowerCase().trim();
        if (lower.contains("ui") || lower.contains("ux") || lower.contains("design")) return "UI/UX Designer";
        if (lower.contains("pm") || lower.contains("기획")) return "Product Manager";
        if (lower.contains("front") || lower.contains("프론트")) return "Frontend Developer";
        if (lower.contains("full") || lower.contains("풀스택")) return "Fullstack Developer";
        return "Backend Developer";
    }
}
