package org.zerock.nextenter.recommendation.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.zerock.nextenter.recommendation.entity.JobRecommendation;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private Long recommendationId;
    private Long resumeId;
    private String resumeTitle;  // 조인해서 가져오거나 별도 조회
    private List<RecommendationJobDto> recommendedJobs;  // TOP 5
    private Integer creditUsed;
    private LocalDateTime createdAt;

    public static RecommendationDto from(JobRecommendation entity, String resumeTitle) {
        ObjectMapper mapper = new ObjectMapper();
        List<RecommendationJobDto> jobs = null;

        try {
            jobs = mapper.readValue(
                    entity.getRecommendedJobs(),
                    new TypeReference<List<RecommendationJobDto>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }

        return RecommendationDto.builder()
                .recommendationId(entity.getRecommendationId())
                .resumeId(entity.getResumeId())
                .resumeTitle(resumeTitle)
                .recommendedJobs(jobs)
                .creditUsed(entity.getCreditUsed())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}