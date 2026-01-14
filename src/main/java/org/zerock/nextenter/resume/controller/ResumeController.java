package org.zerock.nextenter.resume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.resume.dto.*;
import org.zerock.nextenter.resume.service.PortfolioService;
import org.zerock.nextenter.resume.service.ResumeService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Resume", description = "이력서 관리 API")
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final PortfolioService portfolioService;

    // ==================== 이력서 API ====================

    @Operation(summary = "이력서 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<List<ResumeListResponse>> getResumeList(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/resume/list - userId: {}", userId);
        List<ResumeListResponse> resumes = resumeService.getResumeList(userId);
        return ResponseEntity.ok(resumes);
    }

    @Operation(summary = "이력서 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeDetail(
            @Parameter(description = "이력서 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/resume/{} - userId: {}", id, userId);
        ResumeResponse resume = resumeService.getResumeDetail(id, userId);
        return ResponseEntity.ok(resume);
    }

    @Operation(summary = "이력서 파일 업로드")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResume(
            @Parameter(description = "업로드할 이력서 파일 (HWP, PDF, DOCX, XLSX)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long userId
    ) {
        log.info("POST /api/resume/upload - userId: {}, filename: {}",
                userId, file.getOriginalFilename());

        ResumeResponse resume = resumeService.uploadResume(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resume);
    }

    @Operation(summary = "이력서 생성")
    @PostMapping
    public ResponseEntity<Map<String, Long>> createResume(
            @Parameter(description = "이력서 생성 요청 데이터", required = true)
            @RequestBody @Valid ResumeRequest request,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long userId
    ) {
        log.info("POST /api/resume - userId: {}, title: {}", userId, request.getTitle());

        ResumeResponse resume = resumeService.createResume(request, userId);

        Map<String, Long> response = new HashMap<>();
        response.put("resumeId", resume.getResumeId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "이력서 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Long>> updateResume(
            @Parameter(description = "이력서 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "이력서 수정 요청 데이터", required = true)
            @RequestBody @Valid ResumeRequest request,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long userId
    ) {
        log.info("PUT /api/resume/{} - userId: {}", id, userId);

        ResumeResponse resume = resumeService.updateResume(id, request, userId);

        Map<String, Long> response = new HashMap<>();
        response.put("resumeId", resume.getResumeId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이력서 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(
            @Parameter(description = "이력서 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long userId
    ) {
        log.info("DELETE /api/resume/{} - userId: {}", id, userId);

        resumeService.deleteResume(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "deleted");

        return ResponseEntity.ok(response);
    }

    // ==================== 포트폴리오 API ====================

    @Operation(summary = "포트폴리오 파일 업로드", description = "이력서에 포트폴리오 파일을 업로드합니다")
    @PostMapping(value = "/{resumeId}/portfolios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PortfolioUploadResponse> uploadPortfolio(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "업로드할 포트폴리오 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "포트폴리오 설명")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("POST /api/resume/{}/portfolios - userId: {}, filename: {}", 
                 resumeId, userId, file.getOriginalFilename());

        PortfolioUploadResponse response = portfolioService.uploadPortfolio(
                userId, resumeId, file, description);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "포트폴리오 목록 조회", description = "특정 이력서의 모든 포트폴리오를 조회합니다")
    @GetMapping("/{resumeId}/portfolios")
    public ResponseEntity<PortfolioListResponse> getPortfolios(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/resume/{}/portfolios - userId: {}", resumeId, userId);

        PortfolioListResponse response = portfolioService.getPortfoliosByResumeId(userId, resumeId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 상세 조회", description = "특정 포트폴리오의 상세 정보를 조회합니다")
    @GetMapping("/{resumeId}/portfolios/{portfolioId}")
    public ResponseEntity<PortfolioDto> getPortfolio(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/resume/{}/portfolios/{} - userId: {}", resumeId, portfolioId, userId);

        PortfolioDto portfolio = portfolioService.getPortfolio(userId, resumeId, portfolioId);

        return ResponseEntity.ok(portfolio);
    }

    @Operation(summary = "포트폴리오 수정", description = "포트폴리오의 설명이나 표시 순서를 수정합니다")
    @PutMapping("/{resumeId}/portfolios/{portfolioId}")
    public ResponseEntity<PortfolioDto> updatePortfolio(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "포트폴리오 수정 요청", required = true)
            @RequestBody @Valid PortfolioUpdateRequest request,
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("PUT /api/resume/{}/portfolios/{} - userId: {}", resumeId, portfolioId, userId);

        PortfolioDto updated = portfolioService.updatePortfolio(
                userId, resumeId, portfolioId, request);

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 삭제합니다 (파일 및 DB 레코드)")
    @DeleteMapping("/{resumeId}/portfolios/{portfolioId}")
    public ResponseEntity<Map<String, String>> deletePortfolio(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("DELETE /api/resume/{}/portfolios/{} - userId: {}", resumeId, portfolioId, userId);

        portfolioService.deletePortfolio(userId, resumeId, portfolioId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "포트폴리오가 삭제되었습니다");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 파일 다운로드", description = "포트폴리오 파일을 다운로드합니다")
    @GetMapping("/{resumeId}/portfolios/{portfolioId}/download")
    public ResponseEntity<Resource> downloadPortfolio(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "포트폴리오 ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/resume/{}/portfolios/{}/download - userId: {}", 
                 resumeId, portfolioId, userId);

        // 포트폴리오 정보 조회
        PortfolioDto portfolio = portfolioService.getPortfolio(userId, resumeId, portfolioId);

        // 파일 다운로드
        Resource resource = portfolioService.downloadPortfolio(userId, resumeId, portfolioId);

        // 한글 파일명 인코딩
        String encodedFileName = new String(
                portfolio.getFileName().getBytes(StandardCharsets.UTF_8),
                StandardCharsets.ISO_8859_1
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }
}
