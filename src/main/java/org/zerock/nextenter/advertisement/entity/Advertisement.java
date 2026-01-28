package org.zerock.nextenter.advertisement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "advertisements")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Advertisement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long companyId; // 광고를 등록한 기업 ID
    
    @Column(nullable = false, length = 200)
    private String title; // 광고 제목
    
    @Column(nullable = false, length = 500)
    private String description; // 광고 설명
    
    @Column(nullable = false, length = 100)
    private String backgroundColor; // 배경 색상 (Tailwind CSS 클래스)
    
    @Column(nullable = false, length = 100)
    private String buttonText; // 버튼 텍스트
    
    @Column(length = 500)
    private String targetUrl; // 클릭 시 이동할 URL (선택사항)
    
    @Column(length = 100)
    private String targetPage; // 클릭 시 이동할 페이지 메뉴 (선택사항, 예: "matching-sub-1")
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성화 상태
    
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0; // 우선순위 (높을수록 먼저 표시)
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 활성화 상태 변경
    public void changeActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }
    
    // 우선순위 변경
    public void changePriority(int priority) {
        this.priority = priority;
    }
}
