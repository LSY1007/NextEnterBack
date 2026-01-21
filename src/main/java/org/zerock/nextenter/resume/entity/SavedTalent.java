package org.zerock.nextenter.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_talent", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_user_id", "resume_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedTalent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_talent_id")
    private Long savedTalentId;

    @Column(name = "company_user_id", nullable = false)
    private Long companyUserId; // 기업 회원 ID

    @Column(name = "resume_id", nullable = false)
    private Long resumeId; // 저장한 이력서 ID

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
