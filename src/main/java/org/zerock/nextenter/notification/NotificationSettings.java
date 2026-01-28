package org.zerock.nextenter.notification;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long userId;
    
    @Column(nullable = false)
    private String userType; // "INDIVIDUAL" 또는 "COMPANY"
    
    // 기업용 알림 설정
    @Column(nullable = false)
    @Builder.Default
    private Boolean newApplicationNotification = true; // 새로운 지원자 발생 시 알림
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean deadlineNotification = true; // 공고 마감 임박 알림
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean interviewResponseNotification = true; // 면접자의 면접 동의 알림
    
    // 개인용 알림 설정
    @Column(nullable = false)
    @Builder.Default
    private Boolean positionOfferNotification = true; // 받은 포지션 제안 알림
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean interviewOfferNotification = true; // 받은 면접 제안 알림
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean applicationStatusNotification = true; // 지원 상태 변경 알림
}
