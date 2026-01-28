package org.zerock.nextenter.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_provider",
                        columnNames = {"email", "provider"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // ✅ unique 제약조건 제거 - email + provider 조합으로 unique 관리
    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 255)  // 소셜 로그인은 비밀번호 null 가능
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // ✅ 주소 관련 필드
    @Column(length = 255)
    private String address;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    // ✅ 소셜 로그인 관련 필드 - provider가 null이면 일반 회원
    @Column(length = 20)
    private String provider; // NAVER, KAKAO, null(일반회원)

    @Column(name = "provider_id", length = 100)
    private String providerId; // 소셜 로그인 고유 ID

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "withdrawal_verification_code", length = 10)
    private String withdrawalVerificationCode;

    @Column(name = "withdrawal_code_expiry")
    private LocalDateTime withdrawalCodeExpiry;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}