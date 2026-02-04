package org.zerock.nextenter.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class ResumeParserDebug {

    public static void main(String[] args) throws Exception {
        ResumeStructureParser parser = new ResumeStructureParser();
        ObjectMapper objectMapper = new ObjectMapper();

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

        System.out.println("=== Parsed Careers JSON ===");
        System.out.println(careersJson);
        System.out.println("===========================");
    }
}
