package org.zerock.nextenter.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-settings")
@RequiredArgsConstructor
@Log4j2
public class NotificationSettingsController {
    
    private final NotificationSettingsService settingsService;
    
    /**
     * 알림 설정 조회
     */
    @GetMapping("/{userType}/{userId}")
    public ResponseEntity<NotificationSettingsDTO> getSettings(
            @PathVariable String userType,
            @PathVariable Long userId
    ) {
        log.info("알림 설정 조회: userType={}, userId={}", userType, userId);
        NotificationSettings settings = settingsService.getOrCreateSettings(userId, userType.toUpperCase());
        return ResponseEntity.ok(NotificationSettingsDTO.fromEntity(settings));
    }
    
    /**
     * 알림 설정 업데이트
     */
    @PutMapping("/{userType}/{userId}")
    public ResponseEntity<NotificationSettingsDTO> updateSettings(
            @PathVariable String userType,
            @PathVariable Long userId,
            @RequestBody NotificationSettingsDTO dto
    ) {
        log.info("알림 설정 업데이트: userType={}, userId={}, dto={}", userType, userId, dto);
        NotificationSettings settings = settingsService.updateSettings(userId, userType.toUpperCase(), dto);
        return ResponseEntity.ok(NotificationSettingsDTO.fromEntity(settings));
    }
    
    /**
     * 특정 알림 활성화 여부 확인
     */
    @GetMapping("/{userType}/{userId}/check/{notificationType}")
    public ResponseEntity<Map<String, Boolean>> checkNotificationEnabled(
            @PathVariable String userType,
            @PathVariable Long userId,
            @PathVariable String notificationType
    ) {
        boolean enabled = settingsService.isNotificationEnabled(userId, userType.toUpperCase(), notificationType);
        Map<String, Boolean> response = new HashMap<>();
        response.put("enabled", enabled);
        return ResponseEntity.ok(response);
    }
}
