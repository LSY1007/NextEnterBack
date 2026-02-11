package org.zerock.nextenter.apply.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteAppliesRequest {
    @NotEmpty(message = "삭제할 지원 ID 목록은 필수입니다")
    private List<Long> applyIds;
}