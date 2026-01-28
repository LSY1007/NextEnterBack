package org.zerock.nextenter.interviewoffer.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewOfferRequest {

    private Long userId;      // 지원자 ID
    private Long jobId;       // 공고 ID
    private Long applyId;     // 연결된 지원 ID (optional)
}
