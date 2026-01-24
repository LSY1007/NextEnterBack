package org.zerock.nextenter.job.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "job_category", nullable = false, length = 50)
    private String jobCategory;

    @Column(name = "required_skills", columnDefinition = "LONGTEXT")
    private String requiredSkills;

    @Column(name = "preferred_skills", columnDefinition = "LONGTEXT")
    private String preferredSkills;

    @Builder.Default
    @Column(name = "experience_min")
    private Integer experienceMin = 0;

    @Column(name = "experience_max")
    private Integer experienceMax;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(length = 100)
    private String location;

    @Column(name = "location_city", length = 50)
    private String locationCity; // 시/도 정보 (필터링용)

    @Column(columnDefinition = "TEXT")
    private String description;

    // 이미지 URL
    @Column(name = "thumbnail_url", columnDefinition = "LONGTEXT")
    private String thumbnailUrl;

    @Column(name = "detail_image_url", columnDefinition = "LONGTEXT")
    private String detailImageUrl;

    private LocalDate deadline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    // 통계
    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "applicant_count", nullable = false)
    private Integer applicantCount = 0;

    @Builder.Default
    @Column(name = "bookmark_count", nullable = false)
    private Integer bookmarkCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enum 정의
    public enum Status {
        ACTIVE,   // 활성
        CLOSED,   // 마감
        EXPIRED   // 만료
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.applicantCount == null) {
            this.applicantCount = 0;
        }
        if (this.bookmarkCount == null) {
            this.bookmarkCount = 0;
        }
        if (this.status == null) {
            this.status = Status.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}