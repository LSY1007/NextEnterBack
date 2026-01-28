package org.zerock.nextenter.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.zerock.nextenter.job.entity.Bookmark;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkDto {
    private Long bookmarkId;
    private Long jobPostingId;
    private LocalDateTime createdAt;

    public static BookmarkDto from(Bookmark entity) {
        return BookmarkDto.builder()
                .bookmarkId(entity.getBookmarkId())
                .jobPostingId(entity.getJobPostingId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}