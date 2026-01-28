package org.zerock.nextenter.interview.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_annotation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long annotationId;

    @Column(name = "interview_id", nullable = false)
    private Long interviewId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(columnDefinition = "TEXT")
    private String analysisContent; // 분석 내용 (STAR 구조 준수 여부 등)

    private Double specificityScore; // 구체성 점수 (0.0~1.0)
    private Double starComplianceScore; // STAR 구조 준수 점수
    private Double jobFitScore; // 직무 적합성 점수

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
