package org.zerock.nextenter.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "talent_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "company_user_id", nullable = false)
    private Long companyUserId; // 기업 회원 ID

    @Column(name = "resume_id", nullable = false)
    private Long resumeId; // 이력서 ID

    @Column(name = "talent_user_id", nullable = false)
    private Long talentUserId; // 인재(개인) 회원 ID

    @Column(columnDefinition = "TEXT")
    private String message; // 연락 메시지

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
