package org.zerock.nextenter.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_matching")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeMatching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long matchingId;

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "company_name", length = 200)
    private String companyName;

    /** 매칭된 공고 상태 (ACTIVE, CLOSED 등) */
    @Column(name = "job_status", length = 20)
    private String jobStatus;

    @Column(name = "score")
    private Double score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Grade grade;

    /** 이력서 종합 등급 (AI가 평가한 이력서 자체의 등급, 기업 매칭 등급과 별개) */
    @Enumerated(EnumType.STRING)
    @Column(name = "resume_grade", length = 1)
    private Grade resumeGrade;

    @Column(name = "missing_skills", columnDefinition = "LONGTEXT")
    private String missingSkills;

    @Column(columnDefinition = "LONGTEXT")
    private String cons;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "LONGTEXT")
    private String pros;

    /** 주니어/시니어 평가 (경력 3년 이상 시니어, 미만 주니어) */
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 10)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, length = 20)
    @Builder.Default
    private MatchingType matchingType = MatchingType.MANUAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Grade {
        S, A, B, C, F
    }

    public enum ExperienceLevel {
        JUNIOR, SENIOR
    }

    public enum MatchingType {
        MANUAL, AI_RECOMMEND
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.matchingType == null) {
            this.matchingType = MatchingType.MANUAL;
        }
    }
}