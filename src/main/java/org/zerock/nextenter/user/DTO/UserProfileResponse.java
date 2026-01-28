package org.zerock.nextenter.user.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private Integer age;
    private String gender;
    private String profileImage;
    private String bio;
    private String address;  // ✅ 주소 추가
    private String detailAddress;  // ✅ 상세주소 추가
    private String provider;
    private LocalDateTime createdAt;
}