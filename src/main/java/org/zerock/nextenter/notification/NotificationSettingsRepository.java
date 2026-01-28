package org.zerock.nextenter.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
    
    Optional<NotificationSettings> findByUserIdAndUserType(Long userId, String userType);
    
    boolean existsByUserIdAndUserType(Long userId, String userType);
}
