package org.zerock.nextenter.interviewoffer.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewOfferResponse {

    private Long offerId;
    private Long userId;
    private Long jobId;
    private Long companyId;
    private Long applyId;
    
    // 공고 정보
    private String jobTitle;
    private String jobCategory;
    private String companyName;
    private String companyLogoUrl;
    
    // 사용자 정보
    private String userName;
    private Integer userAge;
    
    // 상태 정보
    private String offerType;
    private String interviewStatus;
    private String finalResult;
    
    // 날짜 정보
    private LocalDateTime offeredAt;
    private LocalDateTime respondedAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime updatedAt;
}
