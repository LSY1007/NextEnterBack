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
 * 규칙 기반으로 동작하며, 파싱 실패 시 빈 배열/객체를 반환합니다.
 */
@Component
@Slf4j
public class ResumeStructureParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 스킬 키워드 (이력서에서 매칭 시 skills로 추출) */
    private static final Set<String> SKILL_KEYWORDS = Set.of(
            "java", "python", "javascript", "typescript", "react", "vue", "angular", "node", "spring", "django",
            "flask", "aws", "docker", "kubernetes", "mysql", "postgresql", "mongodb", "redis", "git",
            "kotlin", "swift", "c++", "c#", "go", "rust", "next.js", "html", "css", "tailwind",
            "pytorch", "tensorflow", "langchain", "openai", "llm", "ml", "ai", "nlp"
    );

    private static final Pattern PERIOD_PATTERN = Pattern.compile(
            "(\\d{4})\\s*[.~\\-]?\\s*(\\d{4}|현재)|(\\d{4})\\s*[.\\-]\\s*(\\d{1,2})"
    );

    /**
     * raw text에서 구조화 데이터 추출 후 JSON 문자열로 반환.
     * 파싱 실패 시 빈 JSON 배열/객체 문자열 반환.
     */
    public ParsedResumeStructure parse(String rawText) {
        ParsedResumeStructure out = new ParsedResumeStructure();
        if (rawText == null || rawText.isBlank()) {
            return out;
        }
        String normalized = rawText.replace("\r\n", "\n").trim();
        String lower = normalized.toLowerCase();

        out.setSkills(parseSkills(lower));
        out.setEducations(parseEducations(normalized));
        out.setCareers(parseCareers(normalized));
        out.setExperiences(parseExperiences(normalized));

        return out;
    }

    private String parseSkills(String lowerText) {
        List<String> found = new ArrayList<>();
        for (String keyword : SKILL_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                found.add(keyword);
            }
        }
        if (found.isEmpty()) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(found);
        } catch (JsonProcessingException e) {
            log.warn("skills JSON 직렬화 실패: {}", e.getMessage());
            return "[]";
        }
    }

    private String parseEducations(String text) {
        List<Map<String, String>> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("대학교") || trimmed.contains("대학원") || trimmed.contains("학교") ||
                    trimmed.contains("학과") || trimmed.contains("전공") || trimmed.contains("학위")) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("school", trimmed);
                item.put("major", "");
                item.put("period", extractPeriod(trimmed));
                list.add(item);
            }
        }
        return toJsonArray(list);
    }

    private String parseCareers(String text) {
        List<Map<String, String>> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("회사") || trimmed.contains("근무") || trimmed.contains("경력") ||
                    trimmed.contains("재직") || trimmed.contains("담당") || trimmed.contains("역할")) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("company", trimmed);
                item.put("position", "");
                item.put("role", "");
                item.put("period", extractPeriod(trimmed));
                list.add(item);
            }
        }
        return toJsonArray(list);
    }

    private String parseExperiences(String text) {
        List<Map<String, String>> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("프로젝트") || trimmed.contains("활동") || trimmed.contains("자격") ||
                    trimmed.contains("수상") || trimmed.contains("공모")) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("title", trimmed);
                item.put("period", extractPeriod(trimmed));
                list.add(item);
            }
        }
        return toJsonArray(list);
    }

    private String extractPeriod(String line) {
        Matcher m = PERIOD_PATTERN.matcher(line);
        if (m.find()) {
            return m.group(0).trim();
        }
        return "";
    }

    private String toJsonArray(List<Map<String, String>> list) {
        if (list.isEmpty()) return "[]";
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("JSON 직렬화 실패: {}", e.getMessage());
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
