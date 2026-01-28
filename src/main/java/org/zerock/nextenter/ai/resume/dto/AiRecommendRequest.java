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

        // 이력서 데이터 (String List로 변환)
        List<String> cleanSkills = extractTextList(this.skills);
        List<String> cleanEducations = extractTextList(this.educations);
        List<String> cleanCareers = extractTextList(this.careers);
        List<String> cleanProjects = extractTextList(this.projects);

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

        appendSection(fullTextBuilder, "[경력]", cleanCareers);
        appendSection(fullTextBuilder, "[프로젝트 및 경험]", cleanProjects);
        appendSection(fullTextBuilder, "[학력 사항]", cleanEducations);

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

        // (1) 학력 구조화
        List<Map<String, String>> pythonEdu = new ArrayList<>();
        for (String edu : cleanEducations) {
            Map<String, String> map = new HashMap<>();
            map.put("school_name", edu);
            map.put("major", edu); // 이력이 major에 없다면 임의로 값을 넣어줌
            map.put("degree", "학사"); // 기본값 설정 (null보단 나음)
            map.put("status", "졸업");
            pythonEdu.add(map);
        }
        contentMap.put("education", pythonEdu);

        // (2) 경력 구조화
        double totalYears = years + (months / 12.0);
        double roundedTotalYears = Math.round(totalYears * 10) / 10.0;
        List<Map<String, Object>> pythonCareer = extractCareerList(this.careers, roundedTotalYears);
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

    // 유틸 [Object -> List<String>] 만능 리스트 추출 함수
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
        if (lower.contains("ui") || lower.contains("ux") || lower.contains("design"))
            return "UI/UX Designer";
        if (lower.contains("pm") || lower.contains("기획"))
            return "Product Manager";
        if (lower.contains("front") || lower.contains("프론트"))
            return "Frontend Developer";
        if (lower.contains("full") || lower.contains("풀스택"))
            return "Fullstack Developer";
        return "Backend Developer";
    }

    // ---------------------------------------------------------
    // 경력 정보 구조화 메서드
    // ---------------------------------------------------------

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

                // role 추출 (없으면 jobCategory 기반으로 생성)
                String role = extractField(map, "role", "position", "직무", "position_name");
                if (role == null || role.isEmpty()) {
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
     * key_tasks를 List<String>으로 추출
     * List, String(쉼표/줄바꿈 구분), 또는 단일 값 모두 처리
     */
    private List<String> extractKeyTasks(Map<?, ?> map) {
        List<String> result = new ArrayList<>();

        // 가능한 키 이름들
        String[] keysToCheck = { "key_tasks", "tasks", "주요업무", "responsibilities", "담당업무", "description", "desc" };

        Object tasksValue = null;
        for (String key : keysToCheck) {
            tasksValue = map.get(key);
            if (tasksValue != null) {
                break;
            }
        }

        if (tasksValue == null) {
            return result;
        }

        // List인 경우
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
        // String인 경우 - 쉼표, 줄바꿈, 또는 세미콜론으로 분리
        else if (tasksValue instanceof String) {
            String tasksStr = ((String) tasksValue).trim();
            if (!tasksStr.isEmpty()) {
                // 여러 구분자로 분리 시도
                String[] parts = tasksStr.split("[,\n;]");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            }
        }
        // 기타 타입인 경우 toString() 사용
        else {
            String taskStr = tasksValue.toString().trim();
            if (!taskStr.isEmpty()) {
                result.add(taskStr);
            }
        }

        return result;
    }
}