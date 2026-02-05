package org.zerock.nextenter.resume.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 이력서 raw text(extractedText)를 skills, education, careers, experiences 구조로 파싱합니다.
 * 개선된 버전: 줄바꿈이 있는 설명(description/key_tasks)과 다양한 기간(period) 형식을 지원합니다.
 */
@Component
@Slf4j
public class ResumeStructureParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 스킬 키워드 (이력서에서 매칭 시 skills로 추출) */
    private static final Set<String> SKILL_KEYWORDS = Set.of(
            "java", "python", "javascript", "typescript", "react", "vue", "angular", "node", "spring", "spring boot", "django",
            "flask", "aws", "docker", "kubernetes", "mysql", "postgresql", "mongodb", "redis", "git",
            "kotlin", "swift", "c++", "c#", "go", "rust", "next.js", "html", "css", "tailwind",
            "pytorch", "tensorflow", "langchain", "openai", "llm", "ml", "ai", "nlp", "fastapi",
            "pinecone", "weaviate", "mlops", "fine-tuning", "rag"
    );

    // 날짜 패턴: YYYY.MM, YYYY-MM, YYYY/MM, YYYY.MM.DD 등 지원
    private static final Pattern PERIOD_PATTERN = Pattern.compile(
            "(\\d{4})\\s*[.~\\-/]\\s*(\\d{1,2})(\\s*[.~\\-/]\\s*\\d{1,2})?\\s*[~\\-]\\s*(\\d{4}|현재|present|now)(\\s*[.~\\-/]\\s*\\d{1,2})?(\\s*[.~\\-/]\\s*\\d{1,2})?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * raw text에서 구조화 데이터 추출 후 JSON 문자열로 반환.
     */
    public ParsedResumeStructure parse(String rawText) {
        ParsedResumeStructure out = new ParsedResumeStructure();
        if (rawText == null || rawText.isBlank()) {
            return out;
        }

        // 섹션 분리 (간단히 헤더 기준으로 나눔)
        Map<String, String> sections = splitSections(rawText);

        out.setSkills(parseSkills(sections.getOrDefault("skills", "") + "\n" + rawText)); // 스킬은 전체에서 검색
        out.setEducations(parseEducationSection(sections.get("education")));
        out.setCareers(parseCareerSection(sections.get("career")));
        out.setExperiences(parseExperienceSection(sections.get("experience")));

        return out;
    }

    /**
     * 텍스트를 섹션별로 분리
     */
    private Map<String, String> splitSections(String text) {
        Map<String, String> sections = new HashMap<>();
        String[] lines = text.split("\n");
        String currentSection = "etc";
        StringBuilder buffer = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // 섹션 헤더 감지
            if (isSectionHeader(trimmed)) {
                if (buffer.length() > 0) {
                    sections.put(currentSection, buffer.toString());
                    buffer.setLength(0);
                }
                currentSection = detectSectionType(trimmed);
            } else {
                buffer.append(line).append("\n");
            }
        }
        if (buffer.length() > 0) {
            sections.put(currentSection, buffer.toString());
        }
        return sections;
    }

    private boolean isSectionHeader(String line) {
        return line.matches("^\\[.+].*") || line.matches("^#{1,3}\\s+.+") || 
               (line.length() < 20 && (line.contains("경력") || line.contains("학력") || line.contains("프로젝트") || line.contains("기술") || line.contains("Skills")));
    }

    private String detectSectionType(String header) {
        if (header.contains("학력") || header.contains("Education")) return "education";
        if (header.contains("경력") || header.contains("Work") || header.contains("Career")) return "career";
        if (header.contains("프로젝트") || header.contains("Project") || header.contains("Experience")) return "experience";
        if (header.contains("기술") || header.contains("Skill")) return "skills";
        return "etc";
    }

    private String parseSkills(String text) {
        Set<String> found = new LinkedHashSet<>();
        String lower = text.toLowerCase();
        for (String keyword : SKILL_KEYWORDS) {
            if (lower.contains(keyword)) {
                found.add(keyword);
            }
        }
        return toJson(new ArrayList<>(found));
    }

    /**
     * 경력 섹션 파싱 (회사명, 기간, 역할, 상세내용 추출)
     */
    private String parseCareerSection(String text) {
        if (text == null) return "[]";
        List<Map<String, Object>> list = new ArrayList<>();
        
        List<String> buffer = new ArrayList<>();
        Map<String, Object> currentItem = null;

        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            Matcher pm = PERIOD_PATTERN.matcher(trimmed);
            if (pm.find()) {
                // 날짜 발견! -> 새로운 항목의 시작점(또는 헤더의 일부)
                String period = pm.group(0);
                String rest = trimmed.replace(period, "").trim();

                // 이전 항목 마무리 (버퍼 내용 할당)
                // 단, 버퍼의 마지막 줄이 '회사명'일 가능성 체크
                String candidateCompany = null;
                if (!buffer.isEmpty()) {
                    String last = buffer.get(buffer.size() - 1);
                    // 회사명 후보 조건: 50자 미만, 글머리 기호 없음, 마침표 없음
                    if (last.length() < 50 && !last.startsWith("-") && !last.startsWith("•") && !last.endsWith(".")) {
                        candidateCompany = last;
                        buffer.remove(buffer.size() - 1);
                    }
                }

                if (currentItem != null) {
                    finalizeItem(currentItem, buffer);
                    list.add(currentItem);
                } else if (!buffer.isEmpty()) {
                    // 버퍼 내용이 있지만 currentItem이 없을 때 (첫 항목 이전)
                    // 만약 candidateCompany가 없었다면 버퍼 전체가 이전 잡담.
                    // 만약 candidateCompany가 있었다면 그게 첫 회사명.
                }

                buffer.clear(); // 버퍼 리셋

                // 새 항목 시작
                currentItem = new LinkedHashMap<>();
                currentItem.put("period", period);
                
                if (candidateCompany != null) {
                    currentItem.put("company", candidateCompany);
                }
                
                // 같은 라인에 있는 텍스트 처리
                if (!rest.isEmpty()) {
                    if (currentItem.get("company") == null) {
                        currentItem.put("company", rest);
                    } else {
                        currentItem.put("role", rest);
                    }
                }
            } else {
                buffer.add(trimmed);
            }
        }

        // 마지막 항목 처리
        if (currentItem != null) {
            finalizeItem(currentItem, buffer);
            list.add(currentItem);
        }
        
        return toJson(list);
    }

    private void finalizeItem(Map<String, Object> item, List<String> buffer) {
        if (buffer.isEmpty()) return;
        
        StringBuilder desc = new StringBuilder();
        List<String> keyTasks = new ArrayList<>();

        for (String line : buffer) {
            desc.append(line).append("\n");
            // 키워드 태스크 추출
            if (line.startsWith("-") || line.startsWith("•") || line.startsWith("*") || line.matches("^\\d+\\..+")) {
                keyTasks.add(line.replaceAll("^[-•*\\d.]+\\s*", ""));
            }
        }
        
        item.put("description", desc.toString().trim());
        item.put("key_tasks", keyTasks);
        
        // company가 아직 없으면 Company fill fallback? (No, Keep simple)
        if (item.get("company") == null) {
             item.put("company", "Unknown");
        }
    }

    /**
     * 프로젝트/경험 섹션 파싱
     */
    private String parseExperienceSection(String text) {
        if (text == null) return "[]";
        List<Map<String, Object>> list = new ArrayList<>();
        
        List<String> buffer = new ArrayList<>();
        Map<String, Object> currentItem = null;

        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            Matcher pm = PERIOD_PATTERN.matcher(trimmed);
            if (pm.find()) {
                String period = pm.group(0);
                String rest = trimmed.replace(period, "").trim();

                String candidateTitle = null;
                if (!buffer.isEmpty()) {
                    String last = buffer.get(buffer.size() - 1);
                    if (last.length() < 50 && !last.startsWith("-") && !last.startsWith("•")) {
                        candidateTitle = last;
                        buffer.remove(buffer.size() - 1);
                    }
                }

                if (currentItem != null) {
                    finalizeExperience(currentItem, buffer);
                    list.add(currentItem);
                }
                buffer.clear();

                currentItem = new LinkedHashMap<>();
                currentItem.put("period", period);
                
                if (candidateTitle != null) {
                    currentItem.put("title", candidateTitle);
                }
                
                if (!rest.isEmpty()) {
                    if (currentItem.get("title") == null) {
                        currentItem.put("title", rest);
                    }
                }
            } else {
                buffer.add(trimmed);
            }
        }

        if (currentItem != null) {
            finalizeExperience(currentItem, buffer);
            list.add(currentItem);
        }
        
        return toJson(list);
    }
    
    private void finalizeExperience(Map<String, Object> item, List<String> buffer) {
        if (buffer.isEmpty()) return;
        StringBuilder desc = new StringBuilder();
        for (String line : buffer) {
            desc.append(line).append("\n");
        }
        item.put("description", desc.toString().trim());
        
        if (item.get("title") == null) {
             item.put("title", "Project");
        }
    }

    /**
     * 학력 섹션 파싱
     */
    private String parseEducationSection(String text) {
         if (text == null) return "[]";
        List<Map<String, String>> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            
            if (trimmed.contains("대학") || trimmed.contains("학교") || trimmed.contains("School") || trimmed.contains("Univ")) {
                 Map<String, String> item = new LinkedHashMap<>();
                 item.put("school", trimmed);
                 Matcher m = PERIOD_PATTERN.matcher(trimmed);
                 if (m.find()) {
                     item.put("period", m.group(0));
                     item.put("school", trimmed.replace(m.group(0), "").trim());
                 } else {
                     item.put("period", "");
                 }
                 item.put("major", ""); // 전공 추출은 복잡하므로 일단 공란
                 list.add(item);
            }
        }
        return toJson(list);
    }

    private String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON 변환 실패", e);
            return "[]";
        }
    }

    /** 파싱 결과 DTO */
    public static class ParsedResumeStructure {
        private String skills = "[]";
        private String educations = "[]";
        private String careers = "[]";
        private String experiences = "[]";

        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills != null ? skills : "[]"; }
        public String getEducations() { return educations; }
        public void setEducations(String educations) { this.educations = educations != null ? educations : "[]"; }
        public String getCareers() { return careers; }
        public void setCareers(String careers) { this.careers = careers != null ? careers : "[]"; }
        public String getExperiences() { return experiences; }
        public void setExperiences(String experiences) { this.experiences = experiences != null ? experiences : "[]"; }
    }
}
