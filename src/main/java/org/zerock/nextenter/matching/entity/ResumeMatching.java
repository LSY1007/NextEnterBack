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

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Grade grade;

    @Column(name = "missing_skills", columnDefinition = "LONGTEXT")
    private String missingSkills;

    @Column(columnDefinition = "LONGTEXT")
    private String cons;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "LONGTEXT")
    private String pros;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, length = 20)
    @Builder.Default
    private MatchingType matchingType = MatchingType.MANUAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Grade {
        S, A, B, C, F
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