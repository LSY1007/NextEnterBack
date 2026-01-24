package org.zerock.nextenter.job.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.job.dto.JobPostingListResponse;
import org.zerock.nextenter.job.dto.JobPostingRequest;
import org.zerock.nextenter.job.dto.JobPostingResponse;
import org.zerock.nextenter.job.service.JobPostingService;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Job", description = "공고 관리 API")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @Operation(summary = "공고 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<Page<JobPostingListResponse>> getJobPostingList(
            @Parameter(description = "직무 카테고리 (예: 백엔드, 프론트엔드) - 쉼표로 구분")
            @RequestParam(required = false) String jobCategories,

            @Parameter(description = "지역 - 쉼표로 구분")
            @RequestParam(required = false) String regions,

            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "상태 (ACTIVE, CLOSED, EXPIRED)")
            @RequestParam(required = false) String status,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/jobs/list - jobCategories: {}, regions: {}, keyword: {}, status: {}, page: {}", 
                jobCategories, regions, keyword, status, page);

        Page<JobPostingListResponse> jobs = jobPostingService
                .getJobPostingList(jobCategories, regions, keyword, status, page, size);

        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "공고 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<JobPostingResponse> getJobPostingDetail(
            @Parameter(description = "공고 ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/jobs/{}", id);

        JobPostingResponse job = jobPostingService.getJobPostingDetail(id);
        return ResponseEntity.ok(job);
    }

    @Operation(summary = "공고 등록")
    @PostMapping
    public ResponseEntity<Map<String, Long>> createJobPosting(
            @Parameter(description = "공고 등록 요청 데이터", required = true)
            @RequestBody @Valid JobPostingRequest request,

            @Parameter(description = "기업 ID (companyId)", required = true, example = "1")
            @RequestHeader("companyId") Long companyId
    ) {
        log.info("POST /api/jobs - companyId: {}, title: {}", companyId, request.getTitle());

        JobPostingResponse job = jobPostingService.createJobPosting(request, companyId);

        Map<String, Long> response = new HashMap<>();
        response.put("jobId", job.getJobId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "공고 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Long>> updateJobPosting(
            @Parameter(description = "공고 ID", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "공고 수정 요청 데이터", required = true)
            @RequestBody @Valid JobPostingRequest request,

            @Parameter(description = "기업 ID (companyId)", required = true, example = "1")
            @RequestHeader("companyId") Long companyId
    ) {
        log.info("PUT /api/jobs/{} - companyId: {}", id, companyId);

        JobPostingResponse job = jobPostingService.updateJobPosting(id, request, companyId);

        Map<String, Long> response = new HashMap<>();
        response.put("jobId", job.getJobId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공고 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteJobPosting(
            @Parameter(description = "공고 ID", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "기업 ID (companyId)", required = true, example = "1")
            @RequestHeader("companyId") Long companyId
    ) {
        log.info("DELETE /api/jobs/{} - companyId: {}", id, companyId);

        jobPostingService.deleteJobPosting(id, companyId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "deleted");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공고 상태 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateJobPostingStatus(
            @Parameter(description = "공고 ID", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "기업 ID (companyId)", required = true, example = "1")
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "상태 변경 요청", required = true)
            @RequestBody Map<String, String> request
    ) {
        log.info("PATCH /api/jobs/{}/status - companyId: {}, status: {}", 
                id, companyId, request.get("status"));

        jobPostingService.updateJobPostingStatus(id, companyId, request.get("status"));

        Map<String, String> response = new HashMap<>();
        response.put("message", "success");

        return ResponseEntity.ok(response);
    }
}