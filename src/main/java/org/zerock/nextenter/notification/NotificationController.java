package org.zerock.nextenter.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Log4j2
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 사용자의 모든 알림 조회
     */
    @GetMapping("/{userType}/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @PathVariable String userType,
            @PathVariable Long userId
    ) {
        log.info("알림 조회 요청: userType={}, userId={}", userType, userId);
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId, userType.toUpperCase());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/{userType}/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable String userType,
            @PathVariable Long userId
    ) {
        Long count = notificationService.getUnreadCount(userId, userType.toUpperCase());
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 읽지 않은 알림 목록 조회
     */
    @GetMapping("/{userType}/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @PathVariable String userType,
            @PathVariable Long userId
    ) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId, userType.toUpperCase());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "알림이 읽음 처리되었습니다.");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/{userType}/{userId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @PathVariable String userType,
            @PathVariable Long userId
    ) {
        notificationService.markAllAsRead(userId, userType.toUpperCase());
        Map<String, String> response = new HashMap<>();
        response.put("message", "모든 알림이 읽음 처리되었습니다.");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "알림이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 테스트용 알림 전송
     */
    @PostMapping("/test/{userType}/{userId}")
    public ResponseEntity<Map<String, String>> sendTestNotification(
            @PathVariable String userType,
            @PathVariable Long userId
    ) {
        log.info("테스트 알림 전송 요청: userType={}, userId={}", userType, userId);
        
        notificationService.createAndSendNotification(
            userId,
            userType.toUpperCase(),
            Notification.NotificationType.INTERVIEW_OFFER,
            "테스트 알림",
            "이것은 테스트 알림입니다. 웹소켓이 정상적으로 작동하는지 확인하세요.",
            null,
            null
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "테스트 알림이 전송되었습니다.");
        return ResponseEntity.ok(response);
    }
}
