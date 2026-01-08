package org.zerock.nextenter.recommendation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JobRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    @Column(name = "recommended_jobs", columnDefinition = "LONGTEXT", nullable = false)
    private String recommendedJobs;  // JSON 문자열

    @Column(name = "credit_used", nullable = false)
    @Builder.Default
    private Integer creditUsed = 50;

    @Column(name = "request_data", columnDefinition = "LONGTEXT")
    private String requestData;  // JSON 문자열 (optional)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}