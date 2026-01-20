package org.zerock.nextenter.apply.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyRequest {
    
    @NotNull(message = "공고 ID는 필수입니다")
    private Long jobId;
    
    @NotNull(message = "이력서 ID는 필수입니다")
    private Long resumeId;
    
    private Long coverLetterId; // 선택사항
}
