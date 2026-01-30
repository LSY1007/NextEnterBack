package org.zerock.nextenter.common.constants;

public class JobConstants {

    public static final String BACKEND_DEV = "백엔드 개발자";
    public static final String FRONTEND_DEV = "프론트엔드 개발자";
    public static final String AI_LLM_ENGINEER = "AI/LLM 엔지니어"; // Updated from Data Analyst
    public static final String DATA_ANALYST_LEGACY = "데이터 분석가"; // Legacy
    public static final String DATA_ANALYST_LEGACY_EN = "Data Analyst"; // Legacy EN

    // Add other job categories as needed
    public static final String FULLSTACK_DEV = "풀스택 개발자";
    public static final String DEVOPS_ENGINEER = "DevOps 엔지니어";
    public static final String MOBILE_APP_DEV = "모바일 앱 개발자";
    public static final String PM_PO = "PM/PO";

    /**
     * Normalize job category (e.g. migrate legacy titles)
     */
    public static String normalize(String jobCategory) {
        if (jobCategory == null)
            return null;

        String trimmed = jobCategory.trim();
        if (trimmed.equalsIgnoreCase(DATA_ANALYST_LEGACY) ||
                trimmed.equalsIgnoreCase(DATA_ANALYST_LEGACY_EN)) {
            return AI_LLM_ENGINEER;
        }
        return trimmed;
    }
}
