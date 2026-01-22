package org.zerock.nextenter.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // 사용자의 알림 목록 조회 (최신순)
    List<Notification> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, String userType);
    
    // 읽지 않은 알림 개수 조회
    Long countByUserIdAndUserTypeAndIsReadFalse(Long userId, String userType);
    
    // 읽지 않은 알림 목록 조회
    List<Notification> findByUserIdAndUserTypeAndIsReadFalseOrderByCreatedAtDesc(Long userId, String userType);
    
    // 알림을 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId);
    
    // 모든 알림을 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.userType = :userType")
    void markAllAsRead(@Param("userId") Long userId, @Param("userType") String userType);
    
    // 특정 타입의 알림 조회
    List<Notification> findByUserIdAndUserTypeAndTypeOrderByCreatedAtDesc(Long userId, String userType, Notification.NotificationType type);
}
