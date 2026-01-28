package org.zerock.nextenter.apply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyListResponse {
    private Long applyId;
    private Long userId;
    private Long jobId;

    // 지원자 기본 정보
    private String userName;
    private Integer userAge;

    // 공고 정보
    private String jobTitle;
    private String jobCategory;

    // 이력서 정보
    private List<String> skills;
    private String experience;

    // 지원 정보
    private String status;
    private String interviewStatus;
    private Integer aiScore;

    private LocalDateTime appliedAt;

    // 공고 상세 정보
    private String companyName;
    private String location;
    private String deadline;
}