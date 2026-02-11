package org.zerock.nextenter.apply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.apply.dto.*;
import org.zerock.nextenter.apply.service.ApplyService;
import org.zerock.nextenter.apply.dto.DeleteAppliesRequest;

import java.util.List;

@Tag(name = "Apply", description = "지원 관리 API")
@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
@Slf4j
public class ApplyController {

    private final ApplyService applyService;

    @Operation(summary = "지원하기", description = "개인회원이 채용공고에 지원합니다")
    @PostMapping
    public ResponseEntity<ApplyResponse> createApply(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId,

            @Parameter(description = "지원 요청 데이터", required = true)
            @RequestBody @Valid ApplyRequest request
    ) {
        log.info("POST /api/applies - userId: {}, jobId: {}", userId, request.getJobId());

        ApplyResponse apply = applyService.createApply(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(apply);
    }

    @Operation(summary = "내 지원 내역 조회", description = "개인회원이 자신의 모든 지원 내역을 조회합니다")
    @GetMapping("/my")
    public ResponseEntity<List<ApplyListResponse>> getMyApplies(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/applies/my - userId: {}", userId);

        List<ApplyListResponse> applies = applyService.getMyApplies(userId);

        return ResponseEntity.ok(applies);
    }

    @Operation(summary = "기업 지원자 목록 조회", description = "기업에 지원한 모든 지원자 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<Page<ApplyListResponse>> getApplies(
            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "공고 ID (특정 공고의 지원자만 조회)")
            @RequestParam(required = false) Long jobId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/applies - companyId: {}, jobId: {}", companyId, jobId);

        Page<ApplyListResponse> applies =
                applyService.getAppliesByCompany(companyId, jobId, page, size);

        return ResponseEntity.ok(applies);
    }

    @Operation(summary = "지원자 상세 조회", description = "특정 지원자의 상세 정보를 조회합니다")
    @GetMapping("/{applyId}")
    public ResponseEntity<ApplyResponse> getApplyDetail(
            @Parameter(description = "지원 ID", required = true)
            @PathVariable Long applyId,

            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId
    ) {
        log.info("GET /api/applies/{} - companyId: {}", applyId, companyId);

        ApplyResponse apply = applyService.getApplyDetail(applyId, companyId);

        return ResponseEntity.ok(apply);
    }

    @Operation(summary = "지원 상태 변경", description = "지원자의 합격/불합격 등 상태를 변경합니다")
    @PutMapping("/{applyId}/status")
    public ResponseEntity<ApplyResponse> updateApplyStatus(
            @Parameter(description = "지원 ID", required = true)
            @PathVariable Long applyId,

            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "상태 변경 요청", required = true)
            @RequestBody @Valid ApplyStatusUpdateRequest request
    ) {
        log.info("PUT /api/applies/{}/status - companyId: {}, status: {}",
                applyId, companyId, request.getStatus());

        ApplyResponse apply =
                applyService.updateApplyStatus(applyId, companyId, request);

        return ResponseEntity.ok(apply);
    }

    @Operation(summary = "면접 상태 변경", description = "지원자의 면접 상태를 변경합니다")
    @PutMapping("/{applyId}/interview-status")
    public ResponseEntity<ApplyResponse> updateInterviewStatus(
            @Parameter(description = "지원 ID", required = true)
            @PathVariable Long applyId,

            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "면접 상태 (REQUESTED, ACCEPTED, REJECTED)", required = true)
            @RequestBody String interviewStatus
    ) {
        log.info("PUT /api/applies/{}/interview-status - companyId: {}, interviewStatus: {}",
                applyId, companyId, interviewStatus);

        ApplyResponse apply =
                applyService.updateInterviewStatus(applyId, companyId, interviewStatus);

        return ResponseEntity.ok(apply);
    }

    @Operation(summary = "인재검색에서 면접 요청", description = "기업이 인재검색에서 면접을 요청합니다")
    @PostMapping("/interview-request")
    public ResponseEntity<ApplyResponse> createInterviewRequest(
            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId,

            @Parameter(description = "공고 ID", required = true)
            @RequestParam Long jobId
    ) {
        log.info("POST /api/applies/interview-request - companyId: {}, userId: {}, jobId: {}",
                companyId, userId, jobId);

        ApplyResponse apply = applyService.createInterviewRequest(companyId, userId, jobId);

        return ResponseEntity.status(HttpStatus.CREATED).body(apply);
    }

    @Operation(summary = "내 지원 내역 조회", description = "개인회원이 자신이 지원한 공고 목록을 조회합니다")
    @GetMapping("/my-applications")
    public ResponseEntity<Page<ApplyListResponse>> getMyApplications(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/applies/my-applications - userId: {}", userId);

        Page<ApplyListResponse> applies =
                applyService.getMyApplications(userId, page, size);

        return ResponseEntity.ok(applies);
    }

    // ✅ [추가됨] 지원 취소 API
    @Operation(summary = "지원 취소", description = "개인회원이 지원을 취소합니다")
    @PatchMapping("/{applyId}/cancel")
    public ResponseEntity<Void> cancelApply(
            @Parameter(description = "지원 ID", required = true) @PathVariable Long applyId,
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("userId") Long userId
    ) {
        log.info("PATCH /api/applies/{}/cancel - userId: {}", applyId, userId);
        applyService.cancelApply(userId, applyId);
        return ResponseEntity.ok().build();
    }

    // ✅ [신규] 지원자 일괄 삭제 API
    @Operation(summary = "지원자 삭제", description = "기업이 지원자를 일괄 삭제합니다")
    @DeleteMapping
    public ResponseEntity<Void> deleteApplies(
            @Parameter(description = "기업 ID", required = true)
            @RequestHeader("companyId") Long companyId,

            @Parameter(description = "삭제할 지원 ID 목록", required = true)
            @RequestBody DeleteAppliesRequest request
    ) {
        log.info("DELETE /api/applies - companyId: {}, applyIds: {}", companyId, request.getApplyIds());

        applyService.deleteApplies(companyId, request.getApplyIds());

        return ResponseEntity.noContent().build();
    }
}
