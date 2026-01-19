package org.zerock.nextenter.ai.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_ai_recommend")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAiRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id")
    private Long recommendId;

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    @Column(name = "user_id")
    private Long userId;

    // AI 응답 전체 JSON
    @Column(name = "ai_response", columnDefinition = "LONGTEXT")
    private String aiResponse;

    // ai_report 필드만 추출
    @Column(name = "ai_report", columnDefinition = "LONGTEXT")
    private String aiReport;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
