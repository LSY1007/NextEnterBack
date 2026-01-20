package org.zerock.nextenter.apply.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다")
    private String status; // PENDING, REVIEWING, ACCEPTED, REJECTED

    private String notes; // 기업 측 메모 (선택)
}