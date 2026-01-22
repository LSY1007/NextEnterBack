package org.zerock.nextenter.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsDTO {
    
    private Long id;
    private Long userId;
    private String userType;
    
    // 기업용 알림 설정
    private Boolean newApplicationNotification;
    private Boolean deadlineNotification;
    private Boolean interviewResponseNotification;
    
    // 개인용 알림 설정
    private Boolean positionOfferNotification;
    private Boolean interviewOfferNotification;
    private Boolean applicationStatusNotification;
    
    public static NotificationSettingsDTO fromEntity(NotificationSettings settings) {
        return NotificationSettingsDTO.builder()
                .id(settings.getId())
                .userId(settings.getUserId())
                .userType(settings.getUserType())
                .newApplicationNotification(settings.getNewApplicationNotification())
                .deadlineNotification(settings.getDeadlineNotification())
                .interviewResponseNotification(settings.getInterviewResponseNotification())
                .positionOfferNotification(settings.getPositionOfferNotification())
                .interviewOfferNotification(settings.getInterviewOfferNotification())
                .applicationStatusNotification(settings.getApplicationStatusNotification())
                .build();
    }
}
