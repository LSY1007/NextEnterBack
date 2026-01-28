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

    // ✅ 서류 상태 (기존 status 대체)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false, length = 20)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    // ✅ 최종 결과 (서류 합격 후)
    @Enumerated(EnumType.STRING)
    @Column(name = "final_status", length = 20)
    private FinalStatus finalStatus;

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

    // ✅ 서류 상태 Enum
    public enum DocumentStatus {
        PENDING,    // 서류 검토 대기
        REVIEWING,  // 서류 검토 중
        PASSED,     // 서류 합격
        REJECTED,    // 서류 불합격
        CANCELED    // 지원 취소
    }

    // ✅ 최종 결과 Enum
    public enum FinalStatus {
        PASSED,     // 최종 합격
        REJECTED,   // 최종 불합격
        CANCELED    // 지원 취소
    }

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.documentStatus == null) {
            this.documentStatus = DocumentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
