package org.zerock.nextenter.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationSettingsService {
    
    private final NotificationSettingsRepository settingsRepository;
    
    /**
     * 알림 설정 조회 (없으면 기본값으로 생성)
     */
    @Transactional
    public NotificationSettings getOrCreateSettings(Long userId, String userType) {
        return settingsRepository.findByUserIdAndUserType(userId, userType)
                .orElseGet(() -> {
                    NotificationSettings settings = NotificationSettings.builder()
                            .userId(userId)
                            .userType(userType)
                            .newApplicationNotification(true)
                            .deadlineNotification(true)
                            .interviewResponseNotification(true)
                            .positionOfferNotification(true)
                            .interviewOfferNotification(true)
                            .applicationStatusNotification(true)
                            .build();
                    return settingsRepository.save(settings);
                });
    }
    
    /**
     * 알림 설정 업데이트
     */
    @Transactional
    public NotificationSettings updateSettings(Long userId, String userType, NotificationSettingsDTO dto) {
        NotificationSettings settings = getOrCreateSettings(userId, userType);
        
        // 기업용 설정
        if (dto.getNewApplicationNotification() != null) {
            settings.setNewApplicationNotification(dto.getNewApplicationNotification());
        }
        if (dto.getDeadlineNotification() != null) {
            settings.setDeadlineNotification(dto.getDeadlineNotification());
        }
        if (dto.getInterviewResponseNotification() != null) {
            settings.setInterviewResponseNotification(dto.getInterviewResponseNotification());
        }
        
        // 개인용 설정
        if (dto.getPositionOfferNotification() != null) {
            settings.setPositionOfferNotification(dto.getPositionOfferNotification());
        }
        if (dto.getInterviewOfferNotification() != null) {
            settings.setInterviewOfferNotification(dto.getInterviewOfferNotification());
        }
        if (dto.getApplicationStatusNotification() != null) {
            settings.setApplicationStatusNotification(dto.getApplicationStatusNotification());
        }
        
        return settingsRepository.save(settings);
    }
    
    /**
     * 특정 알림이 활성화되어 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(Long userId, String userType, String notificationType) {
        NotificationSettings settings = getOrCreateSettings(userId, userType);
        
        return switch (notificationType) {
            case "NEW_APPLICATION" -> settings.getNewApplicationNotification();
            case "DEADLINE_APPROACHING" -> settings.getDeadlineNotification();
            case "INTERVIEW_ACCEPTED", "INTERVIEW_REJECTED" -> settings.getInterviewResponseNotification();
            case "POSITION_OFFER" -> settings.getPositionOfferNotification();
            case "INTERVIEW_OFFER" -> settings.getInterviewOfferNotification();
            case "APPLICATION_STATUS" -> settings.getApplicationStatusNotification();
            default -> true;
        };
    }
}
