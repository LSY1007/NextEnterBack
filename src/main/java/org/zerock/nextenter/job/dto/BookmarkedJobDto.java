package org.zerock.nextenter.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedJobDto {
    // 북마크 정보
    private Long bookmarkId;
    private LocalDateTime bookmarkedAt;

    // 공고 정보
    private Long jobPostingId;
    private String title;
    private String companyName;
    private String location;
    private String experienceLevel;
    private String salary;
    private String jobType;
    private LocalDateTime deadline;
    private String status;  // OPEN, CLOSED
}