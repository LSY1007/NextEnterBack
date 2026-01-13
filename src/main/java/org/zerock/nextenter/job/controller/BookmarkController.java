package org.zerock.nextenter.job.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.job.dto.BookmarkDto;
import org.zerock.nextenter.job.dto.BookmarkedJobDto;
import org.zerock.nextenter.job.service.BookmarkService;

import java.util.List;
import java.util.Map;

@Tag(name = "Bookmark", description = "공고 북마크 API")
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 추가", description = "공고를 북마크에 추가합니다")
    @PostMapping("/{jobPostingId}")
    public ResponseEntity<BookmarkDto> addBookmark(
            @RequestParam(required = false) Long userId,  // 임시
            @PathVariable Long jobPostingId) {
        if (userId == null) userId = 1L;
        BookmarkDto result = bookmarkService.addBookmark(userId, jobPostingId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "북마크 삭제", description = "북마크를 삭제합니다")
    @DeleteMapping("/{jobPostingId}")
    public ResponseEntity<Void> removeBookmark(
            @RequestParam(required = false) Long userId,  // 임시
            @PathVariable Long jobPostingId) {
        if (userId == null) userId = 1L;
        bookmarkService.removeBookmark(userId, jobPostingId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "북마크 토글", description = "북마크를 추가/삭제 토글합니다 (있으면 삭제, 없으면 추가)")
    @PutMapping("/{jobPostingId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @RequestParam(required = false) Long userId,  // 임시
            @PathVariable Long jobPostingId) {
        if (userId == null) userId = 1L;
        BookmarkDto result = bookmarkService.toggleBookmark(userId, jobPostingId);

        // 결과 반환 (추가되었는지, 삭제되었는지)
        Map<String, Object> response = Map.of(
                "isBookmarked", result != null,
                "bookmark", result != null ? result : Map.of()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "북마크 목록", description = "사용자가 북마크한 공고 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<Page<BookmarkedJobDto>> getBookmarkedJobs(
            @RequestParam(required = false) Long userId,  // 임시
            Pageable pageable) {
        if (userId == null) userId = 1L;
        Page<BookmarkedJobDto> result = bookmarkService.getBookmarkedJobs(userId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "북마크 여부 확인", description = "특정 공고가 북마크되었는지 확인합니다")
    @GetMapping("/{jobPostingId}/status")
    public ResponseEntity<Map<String, Boolean>> checkBookmarkStatus(
            @RequestParam(required = false) Long userId,  // 임시
            @PathVariable Long jobPostingId) {
        if (userId == null) userId = 1L;
        boolean isBookmarked = bookmarkService.isBookmarked(userId, jobPostingId);
        return ResponseEntity.ok(Map.of("isBookmarked", isBookmarked));
    }

    @Operation(summary = "여러 공고 북마크 여부 확인", description = "여러 공고의 북마크 여부를 한 번에 확인합니다")
    @PostMapping("/status/batch")
    public ResponseEntity<Map<Long, Boolean>> checkMultipleBookmarkStatus(
            @RequestParam(required = false) Long userId,  // 임시
            @RequestBody List<Long> jobPostingIds) {
        if (userId == null) userId = 1L;
        Map<Long, Boolean> result = bookmarkService.getBookmarkStatusMap(userId, jobPostingIds);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "내 북마크 총 개수", description = "사용자의 총 북마크 개수를 조회합니다")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getBookmarkCount(
            @RequestParam(required = false) Long userId) {  // 임시
        if (userId == null) userId = 1L;
        Long count = bookmarkService.getBookmarkCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}