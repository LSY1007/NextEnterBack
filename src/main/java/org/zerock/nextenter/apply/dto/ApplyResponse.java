package org.zerock.nextenter.apply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyResponse {
    private Long applyId;
    private Long userId;
    private Long jobId;
    private Long resumeId;
    private Long coverLetterId;

    // 지원자 정보
    private String userName;
    private Integer userAge;
    private String userEmail;
    private String userPhone;

    // 공고 정보
    private String jobTitle;
    private String jobCategory;

    // 지원 정보
    private String status;
    private Integer aiScore;
    private String notes;

    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
}