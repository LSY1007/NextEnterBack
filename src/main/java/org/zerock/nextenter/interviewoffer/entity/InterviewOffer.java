package org.zerock.nextenter.interviewoffer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// ✅ 삭제(delete) 실행 시 -> 실제로는 업데이트(update) 수행
@SQLDelete(sql = "UPDATE interview_offer SET deleted = true, deleted_at = NOW() WHERE offer_id = ?")
// ✅ 조회(select) 시 -> 삭제 안 된 것만 가져오기
@Where(clause = "deleted = false")
public class InterviewOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "apply_id")
    private Long applyId;

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

    // ✅ [핵심] Boolean(대문자) 사용 -> getDeleted() 메서드 생성됨
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "offered_at", nullable = false, updatable = false)
    private LocalDateTime offeredAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum OfferType { COMPANY_INITIATED, FROM_APPLICATION }
    public enum InterviewStatus { OFFERED, ACCEPTED, REJECTED, SCHEDULED, COMPLETED, CANCELED }
    public enum FinalResult { PASSED, REJECTED }

    @PrePersist
    protected void onCreate() {
        this.offeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.offerType == null) this.offerType = OfferType.COMPANY_INITIATED;
        if (this.interviewStatus == null) this.interviewStatus = InterviewStatus.OFFERED;
        if (this.deleted == null) this.deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}