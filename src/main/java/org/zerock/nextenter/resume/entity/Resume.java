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

    // ===== 개인정보 필드들 (이력서별 개인정보) =====
    
    @Column(name = "resume_name", length = 50)
    private String resumeName; // 이력서에 표시할 이름
    
    @Column(name = "resume_gender", length = 10)
    private String resumeGender; // 성별
    
    @Column(name = "resume_birth_date", length = 20)
    private String resumeBirthDate; // 생년월일
    
    @Column(name = "resume_email", length = 100)
    private String resumeEmail; // 이메일
    
    @Column(name = "resume_phone", length = 20)
    private String resumePhone; // 연락처
    
    @Column(name = "resume_address", length = 200)
    private String resumeAddress; // 주소
    
    @Column(name = "resume_detail_address", length = 100)
    private String resumeDetailAddress; // 상세주소
    
    @Column(name = "profile_image", columnDefinition = "LONGTEXT")
    private String profileImage; // 프로필 이미지 (Base64)

    // ===== 분리된 섹션 컬럼들 (JSON 타입) =====

    // 경험/활동/교육
    @Column(columnDefinition = "JSON")
    private String experiences;
    // 예: [{"title":"자격증명","period":"2020.01 - 2021.01"}]

    // 자격증/어학/수상
    @Column(columnDefinition = "JSON")
    private String certificates;
    // 예: [{"title":"TOEIC 900","date":"2020.01"}]

    // 학력
    @Column(columnDefinition = "JSON")
    private String educations;
    // 예: [{"school":"서울대학교","major":"컴퓨터공학","period":"2015 ~ 2019"}]

    // 경력
    @Column(columnDefinition = "JSON")
    private String careers;
    // 예: [{"company":"네이버","position":"선임연구원","role":"백엔드 개발","period":"2019.01 ~ 2023.01"}]

    // ===== 기존 필드들 =====

    // AI 처리 결과 (LONGTEXT) - 나중에 AI 서버 연동 시 사용
    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    // 기존 structuredData - 점진적 제거를 위해 유지
    @Deprecated
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
    private Visibility visibility = Visibility.PUBLIC;

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
            this.visibility = Visibility.PUBLIC;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}