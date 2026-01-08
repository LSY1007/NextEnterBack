package org.zerock.nextenter.resume.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.resume.dto.ResumeListResponse;
import org.zerock.nextenter.resume.dto.ResumeRequest;
import org.zerock.nextenter.resume.dto.ResumeResponse;
import org.zerock.nextenter.resume.service.ResumeService;

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

    @Operation(summary = "이력서 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<List<ResumeListResponse>> getResumeList(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/codequery - userId: {}", userId);
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
        log.info("GET /api/codequery/{} - userId: {}", id, userId);
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
        log.info("POST /api/codequery/upload - userId: {}, filename: {}",
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
        log.info("POST /api/codequery - userId: {}, title: {}", userId, request.getTitle());

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
        log.info("PUT /api/codequery/{} - userId: {}", id, userId);

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
        log.info("DELETE /api/codequery/{} - userId: {}", id, userId);

        resumeService.deleteResume(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "deleted");

        return ResponseEntity.ok(response);
    }
}