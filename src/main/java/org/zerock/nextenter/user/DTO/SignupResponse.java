package org.zerock.nextenter.user.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {

    private Long userId;
    private String email;
    private String name;
    private Integer age;        // ✅ 나이 추가
    private String gender;      // ✅ 성별 추가
}