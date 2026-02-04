package org.zerock.nextenter.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResumeStructureParserTest {

    private final ResumeStructureParser parser = new ResumeStructureParser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testParseCareersWithDescription() throws Exception {
        String rawText = 
            "[경력]\n" +
            "네이버 클로바\n" +
            "2022.03 ~ 현재\n" +
            "- LLM 모델 파인튜닝 수행\n" +
            "- RAG 시스템 구축\n" +
            "\n" +
            "카카오\n" +
            "2020.01 - 2022.02\n" +
            "백엔드 서버 개발 담당\n";

        ResumeStructureParser.ParsedResumeStructure result = parser.parse(rawText);
        String careersJson = result.getCareers();

        try {
            java.nio.file.Files.writeString(java.nio.file.Paths.get("c:/FuckingNextEnter/NextEnterBack/debug_result.txt"), careersJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Map<String, String>> careers = objectMapper.readValue(careersJson, List.class);
        
        // We expect 2 career items
        assertEquals(2, careers.size(), "Should find 2 career items");

        Map<String, String> item1 = careers.get(0);
        // Current logic might fail these or produce empty description
        assertEquals("네이버 클로바", item1.get("company"));
        assertTrue(item1.containsKey("description") || item1.containsKey("key_tasks"), "Should have description or key_tasks");
        
        // Verify period extraction
        String period1 = item1.get("period");
        assertNotNull(period1);
        assertTrue(period1.contains("2022") || period1.contains("현재"), "Period should be extracted: " + period1);
    }
}
