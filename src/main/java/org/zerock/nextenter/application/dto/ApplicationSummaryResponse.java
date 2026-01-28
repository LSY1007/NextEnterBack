package org.zerock.nextenter.application.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 통합 지원 응답 DTO
 * Apply(일반 지원)와 InterviewOffer(면접 제안)를 하나의 형식으로 표현
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationSummaryResponse {

    // 기본 식별 정보
    private Long id;  // applyId 또는 offerId
    private String type;  // "APPLICATION" 또는 "INTERVIEW_OFFER"
    
    private Long applyId;  // 실제 applyId (type=APPLICATION일 때)
    private Long offerId;  // 실제 offerId (type=INTERVIEW_OFFER일 때)
    
    // 사용자/공고 정보
    private Long userId;
    private Long jobId;
    private String userName;
    private Integer userAge;
    private String jobTitle;
    private String jobCategory;
    private String companyName;
    private String location;
    private String deadline;
    
    // 기술 스택 및 경력
    private List<String> skills;
    private String experience;
    
    // ✅ 프론트엔드 호환성을 위한 필드 (기존 형식)
    private String status;  // "PENDING", "ACCEPTED", "REJECTED" (호환용)
    private String interviewStatus;  // null, "OFFERED", "ACCEPTED", "REJECTED"
    
    // ✅ 새로운 상태 필드 (상세 정보)
    private String documentStatus;  // "PENDING", "REVIEWING", "PASSED", "REJECTED"
    private String finalStatus;     // null, "PASSED", "REJECTED", "CANCELED"
    
    // 점수 및 메모
    private Integer aiScore;
    
    // 날짜 정보
    private LocalDateTime appliedAt;  // 지원일 또는 제안받은 날
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
}
