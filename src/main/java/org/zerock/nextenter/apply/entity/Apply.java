package org.zerock.nextenter.apply.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "apply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apply_id")
    private Long applyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "cover_letter_id")
    private Long coverLetterId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "ai_score")
    private Integer aiScore; // AI 매칭 점수 (0-100)

    @Column(columnDefinition = "TEXT")
    private String notes; // 기업 측 메모

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "interview_status", length = 20)
    private String interviewStatus; // REQUESTED, ACCEPTED, REJECTED

    public enum Status {
        PENDING,   // 검토중
        REVIEWING, // 서류 검토중
        ACCEPTED,  // 합격
        REJECTED,  // 불합격
        CANCELED   // 지원 취소
    }

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}