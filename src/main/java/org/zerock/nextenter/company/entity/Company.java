package org.zerock.nextenter.company.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    // 기업 계정 정보
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name; // 담당자 이름

    @Column(length = 20)
    private String phone;

    // 기업 정보
    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(length = 50)
    private String industry;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "logo_url", columnDefinition = "LONGTEXT")
    private String logoUrl;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 기존 필드들 아래에 추가 * 진규 - 기업회원 마이페이지 생성 추가 컬럼들
    @Column(name = "ceo_name", length = 50)
    private String ceoName;

    @Column(name = "short_intro", length = 200)
    private String shortIntro;

    @Column(name = "sns_url", length = 255)
    private String snsUrl;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "manager_department", length = 100)
    private String managerDepartment;

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