package org.zerock.nextenter.coverletter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cover_letter")
@Getter
@Setter  // ✅ Setter 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoverLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_letter_id")
    private Long coverLetterId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ✅ 이력서와의 연결
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "job_category", length = 50)
    private String jobCategory;

    @Column(name = "target_company", length = 100)
    private String targetCompany;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "word_count", nullable = false)
    @Builder.Default
    private Integer wordCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 비즈니스 메서드
    public void update(String title, String jobCategory, String targetCompany, String content) {
        this.title = title;
        this.jobCategory = jobCategory;
        this.targetCompany = targetCompany;
        this.content = content;
        if (content != null) {
            this.wordCount = content.length();
        }
    }

    public void updateFile(String filePath, String fileType) {
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public void removeFile() {
        this.filePath = null;
        this.fileType = null;
    }
}