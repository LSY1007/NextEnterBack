package org.zerock.nextenter.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TalentSearchResponse {
    private Long resumeId;
    private Long userId;
    private String name; // 마스킹된 이름 (예: 김**)
    private String jobCategory;
    private List<String> skills;
    private String location;
    private Integer experienceYears;
    private String salaryRange;
    private Integer matchScore; // 매칭 점수 (추후 AI로 계산)
    private Boolean isAvailable; // 연락 가능 여부
    private Integer viewCount;
}
