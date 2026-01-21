package org.zerock.nextenter.resume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.resume.dto.*;
import org.zerock.nextenter.resume.entity.TalentContact;
import org.zerock.nextenter.resume.service.PortfolioService;
import org.zerock.nextenter.resume.service.ResumeService;
import org.zerock.nextenter.resume.service.TalentService;

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
    private final TalentService talentService;

    // ==================== 인재 검색 API ====================

    @Operation(summary = "인재 검색", description = "공개된 이력서를 검색합니다 (기업 회원용)")
    @GetMapping("/search")
    public ResponseEntity<Page<TalentSearchResponse>> searchTalents(
            @Parameter(description = "직무 카테고리 (예: 프론트엔드 개발자, 백엔드 개발자)")
            @RequestParam(required = false) String jobCategory,

            @Parameter(description = "검색 키워드 (기술 스택)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/resume/search - jobCategory: {}, keyword: {}, page: {}", 
                jobCategory, keyword, page);

        Page<TalentSearchResponse> talents = resumeService.searchTalents(
                jobCategory, keyword, page, size);

        return ResponseEntity.ok(talents);
    }

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

    @Operation(summary = "공개 이력서 조회 (기업회원용)", description = "공개된 이력서를 조회합니다. 본인 이력서가 아니어도 공개 설정이면 조회 가능합니다.")
    @GetMapping("/public/{id}")
    public ResponseEntity<ResumeResponse> getPublicResumeDetail(
            @Parameter(description = "이력서 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "조회자 사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long viewerId
    ) {
        log.info("GET /api/resume/public/{} - viewerId: {}", id, viewerId);
        ResumeResponse resume = resumeService.getPublicResumeDetail(id);
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

    @Operation(summary = "인재 저장 (북마크)", description = "인재를 저장합니다")
    @PostMapping("/save/{resumeId}")
    public ResponseEntity<Map<String, Object>> saveTalent(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "기업 회원 ID", required = true)
            @RequestHeader("userId") Long companyUserId
    ) {
        log.info("POST /api/resume/save/{} - companyUserId: {}", resumeId, companyUserId);

        boolean saved = talentService.saveTalent(companyUserId, resumeId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", saved);
        response.put("message", saved ? "인재가 저장되었습니다" : "이미 저장된 인재입니다");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "인재 저장 취소", description = "저장된 인재를 삭제합니다")
    @DeleteMapping("/save/{resumeId}")
    public ResponseEntity<Map<String, Object>> unsaveTalent(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "기업 회원 ID", required = true)
            @RequestHeader("userId") Long companyUserId
    ) {
        log.info("DELETE /api/resume/save/{} - companyUserId: {}", resumeId, companyUserId);

        boolean unsaved = talentService.unsaveTalent(companyUserId, resumeId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", unsaved);
        response.put("message", unsaved ? "저장이 취소되었습니다" : "저장되지 않은 인재입니다");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "인재 저장 여부 확인")
    @GetMapping("/save/check/{resumeId}")
    public ResponseEntity<Map<String, Boolean>> checkSaved(
            @Parameter(description = "이력서 ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "기업 회원 ID", required = true)
            @RequestHeader("userId") Long companyUserId
    ) {
        boolean isSaved = talentService.isSaved(companyUserId, resumeId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("saved", isSaved);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "인재 연락하기", description = "인재에게 연락 요청을 보냅니다")
    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> contactTalent(
            @Parameter(description = "연락 요청 데이터", required = true)
            @RequestBody ContactTalentRequest request,
            @Parameter(description = "기업 회원 ID", required = true)
            @RequestHeader("userId") Long companyUserId
    ) {
        log.info("POST /api/resume/contact - companyUserId: {}, resumeId: {}", 
                companyUserId, request.getResumeId());

        TalentContact contact = talentService.contactTalent(
                companyUserId, request.getResumeId(), request.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "연락 요청이 전송되었습니다");
        response.put("contactId", contact.getContactId());

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
