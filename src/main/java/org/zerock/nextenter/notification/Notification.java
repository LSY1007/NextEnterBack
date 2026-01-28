package org.zerock.nextenter.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId; // 알림을 받을 사용자 ID
    
    @Column(nullable = false)
    private String userType; // "INDIVIDUAL" 또는 "COMPANY"
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // 알림 유형
    
    @Column(nullable = false)
    private String title; // 알림 제목
    
    @Column(length = 1000)
    private String content; // 알림 내용
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false; // 읽음 여부
    
    @Column
    private Long relatedId; // 관련 엔티티 ID (지원서 ID, 면접 ID 등)
    
    @Column
    private String relatedType; // 관련 엔티티 타입 (APPLICATION, INTERVIEW, OFFER 등)
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        // 기업용 알림
        NEW_APPLICATION("새로운 지원자"),
        DEADLINE_APPROACHING("공고 마감 임박"),
        INTERVIEW_ACCEPTED("면접 수락"),
        INTERVIEW_REJECTED("면접 거절"),
        
        // 개인용 알림
        POSITION_OFFER("포지션 제안"),
        INTERVIEW_OFFER("면접 제안"),
        APPLICATION_STATUS("지원 상태 변경"),
        INTERVIEW_SCHEDULED("면접 일정 확정");
        
        private final String description;
        
        NotificationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
