package org.zerock.nextenter.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationSettingsService settingsService;
    
    /**
     * 알림 생성 및 웹소켓으로 전송
     */
    @Transactional
    public Notification createAndSendNotification(
            Long userId,
            String userType,
            Notification.NotificationType type,
            String title,
            String content,
            Long relatedId,
            String relatedType
    ) {
        // 알림 설정 확인
        if (!settingsService.isNotificationEnabled(userId, userType, type.name())) {
            log.info("알림이 비활성화되어 있어 전송하지 않음: userId={}, type={}", userId, type);
            return null;
        }
        // 알림 생성
        Notification notification = Notification.builder()
                .userId(userId)
                .userType(userType)
                .type(type)
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        log.info("알림 생성: userId={}, type={}, title={}", userId, type, title);
        
        // 웹소켓으로 실시간 알림 전송
        sendNotificationViaWebSocket(notification);
        
        return notification;
    }
    
    /**
     * 웹소켓으로 알림 전송
     */
    private void sendNotificationViaWebSocket(Notification notification) {
        String destination = String.format("/topic/notifications/%s/%d",
                notification.getUserType().toLowerCase(),
                notification.getUserId());
        
        NotificationDTO dto = NotificationDTO.fromEntity(notification);
        messagingTemplate.convertAndSend(destination, dto);
        log.info("웹소켓 알림 전송: {}", destination);
    }
    
    /**
     * 사용자의 모든 알림 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId, String userType) {
        return notificationRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(userId, userType)
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }
    
    /**
     * 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId, String userType) {
        return notificationRepository.countByUserIdAndUserTypeAndIsReadFalse(userId, userType);
    }
    
    /**
     * 읽지 않은 알림 목록
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId, String userType) {
        return notificationRepository.findByUserIdAndUserTypeAndIsReadFalseOrderByCreatedAtDesc(userId, userType)
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }
    
    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
        log.info("알림 읽음 처리: notificationId={}", notificationId);
    }
    
    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId, String userType) {
        notificationRepository.markAllAsRead(userId, userType);
        log.info("모든 알림 읽음 처리: userId={}, userType={}", userId, userType);
    }
    
    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("알림 삭제: notificationId={}", notificationId);
    }
    
    // ====== 특정 이벤트에 대한 알림 생성 메서드 ======
    
    /**
     * 새로운 지원자 알림 (기업용)
     */
    @Transactional
    public void notifyNewApplication(Long companyId, String jobTitle, Long applicationId) {
        createAndSendNotification(
                companyId,
                "COMPANY",
                Notification.NotificationType.NEW_APPLICATION,
                "새로운 지원자",
                String.format("'%s' 포지션에 새로운 지원자가 있습니다.", jobTitle),
                applicationId,
                "APPLICATION"
        );
    }
    
    /**
     * 공고 마감 임박 알림 (기업용)
     */
    @Transactional
    public void notifyDeadlineApproaching(Long companyId, String jobTitle, Long jobId, int daysLeft) {
        createAndSendNotification(
                companyId,
                "COMPANY",
                Notification.NotificationType.DEADLINE_APPROACHING,
                "공고 마감 임박",
                String.format("'%s' 포지션이 %d일 후 마감됩니다.", jobTitle, daysLeft),
                jobId,
                "JOB"
        );
    }
    
    /**
     * 면접 수락 알림 (기업용)
     */
    @Transactional
    public void notifyInterviewAccepted(Long companyId, String candidateName, Long interviewId) {
        createAndSendNotification(
                companyId,
                "COMPANY",
                Notification.NotificationType.INTERVIEW_ACCEPTED,
                "면접 수락",
                String.format("%s님이 면접 제안을 수락했습니다.", candidateName),
                interviewId,
                "INTERVIEW"
        );
    }
    
    /**
     * 면접 거절 알림 (기업용)
     */
    @Transactional
    public void notifyInterviewRejected(Long companyId, String candidateName, Long interviewId) {
        createAndSendNotification(
                companyId,
                "COMPANY",
                Notification.NotificationType.INTERVIEW_REJECTED,
                "면접 거절",
                String.format("%s님이 면접 제안을 거절했습니다.", candidateName),
                interviewId,
                "INTERVIEW"
        );
    }
    
    /**
     * 포지션 제안 알림 (개인용)
     */
    @Transactional
    public void notifyPositionOffer(Long userId, String companyName, String jobTitle, Long jobId) {
        createAndSendNotification(
                userId,
                "INDIVIDUAL",
                Notification.NotificationType.POSITION_OFFER,
                "포지션 제안",
                String.format("%s에서 '%s' 포지션을 제안했습니다.", companyName, jobTitle),
                jobId,
                "JOB"
        );
    }
    
    /**
     * 면접 제안 알림 (개인용)
     */
    @Transactional
    public void notifyInterviewOffer(Long userId, String companyName, String jobTitle, Long interviewId) {
        createAndSendNotification(
                userId,
                "INDIVIDUAL",
                Notification.NotificationType.INTERVIEW_OFFER,
                "면접 제안",
                String.format("%s에서 '%s' 포지션 면접을 제안했습니다.", companyName, jobTitle),
                interviewId,
                "INTERVIEW"
        );
    }
    
    /**
     * 지원 상태 변경 알림 (개인용)
     */
    @Transactional
    public void notifyApplicationStatus(Long userId, String companyName, String status, Long applicationId) {
        createAndSendNotification(
                userId,
                "INDIVIDUAL",
                Notification.NotificationType.APPLICATION_STATUS,
                "지원 상태 변경",
                String.format("%s 지원서가 '%s'(으)로 변경되었습니다.", companyName, status),
                applicationId,
                "APPLICATION"
        );
    }
}
