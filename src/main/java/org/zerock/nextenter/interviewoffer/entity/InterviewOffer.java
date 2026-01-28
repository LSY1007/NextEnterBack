package org.zerock.nextenter.interviewoffer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 지원자

    @Column(name = "job_id", nullable = false)
    private Long jobId;   // 공고

    @Column(name = "company_id", nullable = false)
    private Long companyId;  // 기업

    @Column(name = "apply_id")
    private Long applyId;  // 일반 지원과 연결 (nullable)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type", nullable = false, length = 30)
    private OfferType offerType = OfferType.COMPANY_INITIATED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_status", nullable = false, length = 20)
    private InterviewStatus interviewStatus = InterviewStatus.OFFERED;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_result", length = 20)
    private FinalResult finalResult;

    @Column(name = "offered_at", nullable = false, updatable = false)
    private LocalDateTime offeredAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enum 정의
    public enum OfferType {
        COMPANY_INITIATED,   // 기업이 인재검색에서 직접 제안
        FROM_APPLICATION     // 일반 지원 후 기업이 면접 제안
    }

    public enum InterviewStatus {
        OFFERED,      // 면접 제안됨
        ACCEPTED,     // 지원자가 수락
        REJECTED,     // 지원자가 거절
        SCHEDULED,    // 면접 일정 확정
        COMPLETED,    // 면접 완료
        CANCELED      // 기업이 취소
    }

    public enum FinalResult {
        PASSED,       // 최종 합격
        REJECTED      // 최종 불합격
    }

    @PrePersist
    protected void onCreate() {
        this.offeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.offerType == null) {
            this.offerType = OfferType.COMPANY_INITIATED;
        }
        if (this.interviewStatus == null) {
            this.interviewStatus = InterviewStatus.OFFERED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
