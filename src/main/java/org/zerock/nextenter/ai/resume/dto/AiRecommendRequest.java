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
    
    // AI 서버 요청 형식으로 변환
    public Map<String, Object> toAiRequestMap() {
        return Map.of(
            "job_category", jobCategory != null ? jobCategory : "",
            "skills", skills != null ? skills : List.of(),
            "experience", experience != null ? experience : 0,
            "education", education != null ? education : "",
            "preferred_location", preferredLocation != null ? preferredLocation : ""
        );
    }
}
