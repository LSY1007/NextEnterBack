package org.zerock.nextenter.resume.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    // 파일 관련
    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "file_type", length = 20)
    private String fileType;

    // AI 처리 결과 (LONGTEXT) - 나중에 AI 서버 연동 시 사용
    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "structured_data", columnDefinition = "LONGTEXT")
    private String structuredData;

    // 직무 및 스킬
    @Column(name = "job_category", length = 50)
    private String jobCategory;

    @Column(columnDefinition = "LONGTEXT")
    private String skills;

    // 메타 정보
    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private Boolean isMain = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;  // ✅ 기본값 PUBLIC으로 변경

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    // AI 추천 (LONGTEXT) - 나중에 추천 기능 구현 시 사용
    @Column(name = "resume_recommend", columnDefinition = "LONGTEXT")
    private String resumeRecommend;

    // 상태
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";  // DRAFT, COMPLETED

    // 타임스탬프
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Enum 정의
    public enum Visibility {
        PRIVATE,
        PUBLIC
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.isMain == null) {
            this.isMain = false;
        }
        if (this.visibility == null) {
            this.visibility = Visibility.PUBLIC;  // ✅ 기본값 PUBLIC
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}