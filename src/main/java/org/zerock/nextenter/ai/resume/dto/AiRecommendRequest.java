package org.zerock.nextenter.ai.resume.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    @JsonAlias({ "content" })
    private Object resumeText;

    private String jobCategory; // 희망 직무

    // [스킬]
    @Builder.Default
    @JsonAlias({ "skill", "skills" })
    private Object skills = new ArrayList<>();

    private Integer experience; // 년
    private Integer experienceMonths; // 개월

    // [학력]
    @Builder.Default
    @JsonAlias({ "education", "educations" })
    private Object educations = new ArrayList<>();

    // [경력]
    @Builder.Default
    @JsonAlias({ "career", "careers", "professional_experience", "professional_experiences", "work_experience" })
    private Object careers = new ArrayList<>();

    // [프로젝트]
    @Builder.Default
    @JsonAlias({ "project", "projects", "activities", "experiences", "project_experience", "project_experiences" })
    private Object projects = new ArrayList<>();

    private String preferredLocation;

    // [New] 파일 경로 (PDF/DOCX)
    private String filePath;

    // ---------------------------------------------------------
    // AI 이력서 변환 로직
    // ---------------------------------------------------------
    public Map<String, Object> toAiFormat() {
        Map<String, Object> result = new HashMap<>();

        result.put("id", resumeId != null ? String.valueOf(resumeId) : "unknown");
        result.put("target_role", convertJobCategoryToRole(this.jobCategory));

        // [New] 파일 경로 추가 (AI 서버가 파싱할 수 있도록)
        if (this.filePath != null && !this.filePath.isEmpty()) {
            result.put("file_path", this.filePath);
        }

        // 이력서 데이터 (String List로 변환 - 텍스트 요약용)
        List<String> cleanSkills = extractTextList(this.skills);
        List<String> cleanEducationsText = extractTextList(this.educations);
        List<String> cleanCareersText = extractTextList(this.careers);
        List<String> cleanProjectsText = extractTextList(this.projects);

        // 1. [raw_text 통합] AI가 이를 전체 텍스트로 인식
        StringBuilder fullTextBuilder = new StringBuilder();
        String extractedResumeBody = extractString(this.resumeText);
        if (extractedResumeBody != null && !extractedResumeBody.isEmpty()) {
            fullTextBuilder.append(extractedResumeBody);
        }

        // 및 경력 기간 텍스트화
        int years = (this.experience != null) ? this.experience : 0;
        int months = (this.experienceMonths != null) ? this.experienceMonths : 0;
        if (years > 0 || months > 0) {
            fullTextBuilder.append("\n\n[총 경력] ").append(years).append("년").append(months).append("개월");
        }

        appendSection(fullTextBuilder, "[경력]", cleanCareersText);
        appendSection(fullTextBuilder, "[프로젝트 및 경험]", cleanProjectsText);
        appendSection(fullTextBuilder, "[학력 사항]", cleanEducationsText);

        if (!cleanSkills.isEmpty()) {
            fullTextBuilder.append("\n\n[보유 기술]\n").append(String.join(", ", cleanSkills));
        }

        String finalRawText = fullTextBuilder.toString();

        // 2. resume_content 구성
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("raw_text", finalRawText);
        Map<String, Object> skillsDict = new HashMap<>();
        skillsDict.put("essential", new ArrayList<>(cleanSkills));
        skillsDict.put("additional", new ArrayList<>());
        contentMap.put("skills", skillsDict);

        // (1) 학력 구조화 (개선됨)
        List<Map<String, String>> pythonEdu = extractEducationList(this.educations);
        contentMap.put("education", pythonEdu);

        // (2) 경력 구조화
        double totalYears = years + (months / 12.0);
        double roundedTotalYears = Math.round(totalYears * 10) / 10.0;
        List<Map<String, Object>> pythonCareer = extractCareerList(this.careers, roundedTotalYears);
        contentMap.put("professional_experience", pythonCareer);

        // (3) 프로젝트 구조화 (개선됨)
        List<Map<String, String>> pythonProject = extractProjectList(this.projects);
        contentMap.put("project_experience", pythonProject);
        
        result.put("resume_content", contentMap);

        return result;
    }

    // 유틸 [Object -> String] 추출 함수
    private String extractString(Object input) {
        if (input == null)
            return null;
        if (input instanceof String)
            return (String) input;
        if (input instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) input;
            Object content = map.get("content");
            if (content != null) {
                return String.valueOf(content);
            }
            List<String> values = new ArrayList<>();
            for (Object val : map.values()) {
                if (val != null)
                    values.add(val.toString());
            }
            return String.join("\n", values);
        }
        return input.toString();
    }

    // 유틸 [Object -> List<String>] 만능 리스트 추출 함수 (텍스트 요약용)
    private List<String> extractTextList(Object input) {
        List<String> result = new ArrayList<>();
        if (input == null)
            return result;

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
        if (item == null)
            return;

        if (item instanceof String) {
            String s = ((String) item).trim();
            if (!s.isEmpty())
                result.add(s);
        } else if (item instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) item;
            List<String> values = new ArrayList<>();

            // 가능한 모든 키워드 검색
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
        if (items != null && !items.isEmpty()) {
            builder.append("\n\n").append(title).append("\n");
            for (String item : items) {
                builder.append("- ").append(item).append("\n");
            }
        }
    }

    private String convertJobCategoryToRole(String category) {
        if (category == null)
            return "Backend Developer";

        String lower = category.toLowerCase().trim();

        // 1. AI/LLM Engineer
        if (lower.contains("ai") || lower.contains("llm") || lower.contains("data") || 
            lower.contains("ml") || lower.contains("deep") || lower.contains("인공지능")) {
            return "AI/LLM Engineer";
        }

        // 2. PM (Product Manager)
        if (lower.contains("pm") || lower.contains("product") || lower.contains("기획") || 
            lower.contains("manager") || lower.contains("po")) {
            return "PM (Product Manager)";
        }

        // 3. UI/UX Designer
        if (lower.contains("ui") || lower.contains("ux") || lower.contains("design") || lower.contains("디자인")) {
            return "UI/UX Designer";
        }

        // 4. Fullstack Developer
        if (lower.contains("full") || lower.contains("풀스택")) {
            return "Fullstack Developer";
        }

        // 5. Frontend Developer
        if (lower.contains("front") || lower.contains("프론트") || lower.contains("web") || lower.contains("웹")) {
            return "Frontend Developer";
        }

        // 6. Backend Developer (Default)
        return "Backend Developer";
    }

    // ---------------------------------------------------------
    // 구조화된 데이터 추출 메서드 (Map 유지)
    // ---------------------------------------------------------

    /**
     * 학력 정보를 구조화된 Map 리스트로 추출
     */
    private List<Map<String, String>> extractEducationList(Object educations) {
        List<Map<String, String>> result = new ArrayList<>();
        if (educations == null) return result;

        List<Object> eduItems = new ArrayList<>();
        if (educations instanceof Iterable) {
            for (Object item : (Iterable<?>) educations) {
                eduItems.add(item);
            }
        } else {
            eduItems.add(educations);
        }

        for (Object item : eduItems) {
            Map<String, String> map = new HashMap<>();
            
            if (item instanceof Map) {
                Map<?, ?> srcMap = (Map<?, ?>) item;
                
                String school = extractField(srcMap, "school", "schoolName", "school_name", "학교명");
                String major = extractField(srcMap, "major", "majorName", "department", "전공");
                String degree = extractField(srcMap, "degree", "degreeType", "학위");
                String status = extractField(srcMap, "status", "graduationStatus", "졸업여부");

                if (school != null) {
                    map.put("school_name", school);
                    map.put("major", major != null ? major : school); // 전공 없으면 학교명이라도
                    map.put("degree", degree != null ? degree : "학사");
                    map.put("status", status != null ? status : "졸업");
                    result.add(map);
                }
            } else if (item instanceof String) {
                String s = ((String) item).trim();
                if (!s.isEmpty()) {
                    map.put("school_name", s);
                    map.put("major", s);
                    map.put("degree", "학사");
                    map.put("status", "졸업");
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * 프로젝트 정보를 구조화된 Map 리스트로 추출
     */
    private List<Map<String, String>> extractProjectList(Object projects) {
        List<Map<String, String>> result = new ArrayList<>();
        if (projects == null) return result;

        List<Object> projItems = new ArrayList<>();
        if (projects instanceof Iterable) {
            for (Object item : (Iterable<?>) projects) {
                projItems.add(item);
            }
        } else {
            projItems.add(projects);
        }

        for (Object item : projItems) {
            Map<String, String> map = new HashMap<>();
            
            if (item instanceof Map) {
                Map<?, ?> srcMap = (Map<?, ?>) item;
                
                String title = extractField(srcMap, "title", "projectName", "project_title", "name", "프로젝트명");
                String desc = extractField(srcMap, "description", "desc", "details", "내용");
                
                if (title != null) {
                    map.put("project_title", title);
                    map.put("description", desc != null ? desc : title);
                    result.add(map);
                }
            } else if (item instanceof String) {
                String s = ((String) item).trim();
                if (!s.isEmpty()) {
                    map.put("project_title", s);
                    map.put("description", s);
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * 경력 정보를 구조화된 Map 리스트로 추출
     */
    private List<Map<String, Object>> extractCareerList(Object careers, double experienceYears) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (careers == null)
            return result;

        List<Object> careerItems = new ArrayList<>();
        if (careers instanceof Iterable) {
            for (Object item : (Iterable<?>) careers) {
                careerItems.add(item);
            }
        } else {
            careerItems.add(careers);
        }

        for (Object item : careerItems) {
            Map<String, Object> careerMap = new HashMap<>();

            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;

                // company_name 추출
                String companyName = extractField(map, "company", "companyName", "company_name", "회사명");
                if (companyName == null || companyName.isEmpty()) {
                    // Map의 첫 번째 값 사용
                    for (Object val : map.values()) {
                        if (val != null && !val.toString().trim().isEmpty()) {
                            companyName = val.toString().trim();
                            break;
                        }
                    }
                }
                careerMap.put("company_name", companyName != null ? companyName : "Unknown");

                // role 추출 (개선된 로직: 너무 긴 텍스트나 설명문은 제외)
                String role = extractField(map, "role", "position", "직무", "position_name", "job_title");
                
                // 유효성 검사: role이 null이거나, 너무 길거나(50자 초과), 줄바꿈/특수문자가 포함된 경우 무효화
                if (role == null || role.length() > 50 || role.contains("\n") || role.contains("Key Tasks") || role.contains("- ")) {
                    // role이 유효하지 않으면 jobCategory 사용
                    role = convertJobCategoryToRole(this.jobCategory);
                }
                
                careerMap.put("role", role);

                // period 추출
                String period = extractField(map, "period", "duration", "기간", "work_period", "근무기간");
                careerMap.put("period", period != null ? period : null);

                // key_tasks 추출
                List<String> keyTasks = extractKeyTasks(map);
                careerMap.put("key_tasks", keyTasks);

                careerMap.put("experience_years", experienceYears);

            } else if (item instanceof String) {
                // String인 경우 fallback 처리
                String careerStr = ((String) item).trim();
                if (!careerStr.isEmpty()) {
                    careerMap.put("company_name", careerStr);
                    careerMap.put("role", convertJobCategoryToRole(this.jobCategory));
                    careerMap.put("period", null);
                    careerMap.put("key_tasks", new ArrayList<>());
                    careerMap.put("experience_years", experienceYears);
                }
            }

            if (!careerMap.isEmpty()) {
                result.add(careerMap);
            }
        }
        return result;
    }

    /**
     * Map에서 여러 가능한 키 이름으로 필드 추출
     */
    private String extractField(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                String strValue = value.toString().trim();
                if (!strValue.isEmpty()) {
                    return strValue;
                }
            }
        }
        return null;
    }

    /**
     * key_tasks를 List<String>으로 추출 (강화된 버전)
     */
    private List<String> extractKeyTasks(Map<?, ?> map) {
        List<String> result = new ArrayList<>();

        // 1. 명시적인 키 확인 (우선순위 높음)
        String[] explicitKeys = { "key_tasks", "tasks", "주요업무", "responsibilities", "담당업무" };
        Object tasksValue = null;
        for (String key : explicitKeys) {
            tasksValue = map.get(key);
            if (tasksValue != null) break;
        }

        // 2. 명시적 키가 없으면, 설명(description) 필드에서 추출 시도
        if (tasksValue == null) {
             String[] descKeys = { "description", "desc", "details", "내용", "role" }; // role에 잘못 들어간 내용도 구출 시도
             for (String key : descKeys) {
                 Object val = map.get(key);
                 // 텍스트가 길고(50자 이상) '다'나 '함'으로 끝나지 않거나, '-'가 있으면 업무 내용일 확률 높음
                 if (val != null && val.toString().length() > 30) {
                     tasksValue = val;
                     break;
                 }
             }
        }

        // 3. (필살기) 키 이름 상관없이 Map의 모든 값을 뒤져서 긴 텍스트 찾기
        if (tasksValue == null) {
            for (Object val : map.values()) {
                if (val != null) {
                    String s = val.toString().trim();
                    // 30자 이상이고, 날짜나 회사명 형식이 아닌 것
                    if (s.length() > 30 && !s.matches(".*\\d{4}.*") && !s.equals(map.get("company_name"))) {
                        tasksValue = s;
                        break;
                    }
                }
            }
        }

        if (tasksValue == null) {
            return result;
        }

        // List인 경우 그대로 추가
        if (tasksValue instanceof Iterable) {
            for (Object task : (Iterable<?>) tasksValue) {
                if (task != null) {
                    String taskStr = task.toString().trim();
                    if (!taskStr.isEmpty()) {
                        result.add(taskStr);
                    }
                }
            }
        }
        // String인 경우 - 스마트 분리 (줄바꿈, 마침표, 글머리 기호)
        else if (tasksValue instanceof String) {
            String tasksStr = ((String) tasksValue).trim();
            if (!tasksStr.isEmpty()) {
                // (1) " - " 또는 " -" 패턴으로 시작하는 경우 분리
                if (tasksStr.contains("- ") || tasksStr.contains("\n") || tasksStr.contains("•")) {
                     String[] parts = tasksStr.split("(?=\\s*[-•]\\s)|(?<=[.?!]\\s)|[\n]");
                     for (String part : parts) {
                         String clean = part.replaceAll("^\\s*[-•]\\s*", "").trim();
                         if (!clean.isEmpty() && clean.length() > 2) { // 너무 짧은 잡음 제거
                             result.add(clean);
                         }
                     }
                } else {
                    // (2) 구분자가 딱히 없으면 통으로 넣되, 너무 길면 마침표로 끊기 시도
                    result.add(tasksStr);
                }
            }
        }
        else {
            String taskStr = tasksValue.toString().trim();
            if (!taskStr.isEmpty()) {
                result.add(taskStr);
            }
        }

        return result;
    }
}