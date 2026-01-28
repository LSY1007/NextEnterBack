package org.zerock.nextenter.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    
    private Long id;
    private Long userId;
    private String userType;
    private String type;
    private String typeDescription;
    private String title;
    private String content;
    private Boolean isRead;
    private Long relatedId;
    private String relatedType;
    private LocalDateTime createdAt;
    
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .userType(notification.getUserType())
                .type(notification.getType().name())
                .typeDescription(notification.getType().getDescription())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
