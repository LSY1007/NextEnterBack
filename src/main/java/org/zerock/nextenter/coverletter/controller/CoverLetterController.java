package org.zerock.nextenter.coverletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.coverletter.dto.CoverLetterDto;
import org.zerock.nextenter.coverletter.dto.CoverLetterRequest;
import org.zerock.nextenter.coverletter.dto.CoverLetterUploadResponse;
import org.zerock.nextenter.coverletter.service.CoverLetterFileService;
import org.zerock.nextenter.coverletter.service.CoverLetterService;

@Tag(name = "CoverLetter", description = "자소서 관리 API")
@RestController
@RequestMapping("/api/coverletters")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;
    private final CoverLetterFileService coverLetterFileService;

    @Operation(summary = "자소서 목록 조회", description = "사용자의 자소서 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<Page<CoverLetterDto>> getList(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        // 테스트용: userId가 없으면 1로 설정
        if (userId == null) {
            userId = 1L;
        }

        // Sort 방향 설정
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Pageable 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<CoverLetterDto> result = coverLetterService.getList(userId, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "자소서 상세 조회", description = "자소서 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<CoverLetterDto> getDetail(
            @RequestParam(required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            userId = 1L;
        }
        CoverLetterDto result = coverLetterService.getDetail(userId, id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "자소서 작성", description = "새로운 자소서를 작성합니다")
    @PostMapping
    public ResponseEntity<Long> create(
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody CoverLetterRequest request) {
        if (userId == null) {
            userId = 1L;
        }
        Long coverLetterId = coverLetterService.create(userId, request);
        return ResponseEntity.ok(coverLetterId);
    }

    @Operation(summary = "자소서 수정", description = "자소서를 수정합니다")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @RequestParam(required = false) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CoverLetterRequest request) {
        if (userId == null) {
            userId = 1L;
        }
        coverLetterService.update(userId, id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자소서 삭제", description = "자소서를 삭제합니다 (파일 포함)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestParam(required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            userId = 1L;
        }
        coverLetterService.delete(userId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "파일 업로드", description = "PDF/DOCX/HWP 파일을 업로드합니다 (최대 10MB)")
    @PostMapping("/upload")
    public ResponseEntity<CoverLetterUploadResponse> uploadFile(
            @RequestParam(required = false) Long userId,
            @RequestParam("file") MultipartFile file) {
        if (userId == null) {
            userId = 1L;
        }
        CoverLetterUploadResponse response = coverLetterFileService.uploadFile(userId, file);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "파일 다운로드", description = "자소서 파일을 다운로드합니다")
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam(required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            userId = 1L;
        }
        
        // ✅ 자기소개서 정보 조회
        CoverLetterDto coverLetter = coverLetterService.getDetail(userId, id);
        
        // ✅ 파일 다운로드
        Resource resource = coverLetterFileService.downloadFile(userId, id);
        
        // ✅ 파일 타입에 따른 Content-Type 설정
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (coverLetter.getFileType() != null) {
            String fileType = coverLetter.getFileType().toLowerCase();
            if (fileType.equals("pdf")) {
                contentType = MediaType.APPLICATION_PDF;
            } else if (fileType.equals("docx")) {
                contentType = MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            } else if (fileType.equals("hwp")) {
                contentType = MediaType.valueOf("application/x-hwp");
            }
        }
        
        // ✅ 파일명에 확장자 포함 (파일명.확장자)
        String fileName = coverLetter.getTitle();
        if (!fileName.toLowerCase().endsWith("." + coverLetter.getFileType().toLowerCase())) {
            fileName = fileName + "." + coverLetter.getFileType();
        }
        
        // ✅ 한글 파일명 인코딩
        String encodedFileName = new String(
                fileName.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.ISO_8859_1);

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }

    @Operation(summary = "파일 삭제", description = "자소서의 첨부 파일만 삭제합니다")
    @DeleteMapping("/{id}/file")
    public ResponseEntity<Void> deleteFile(
            @RequestParam(required = false) Long userId,
            @PathVariable Long id) {
        if (userId == null) {
            userId = 1L;
        }
        coverLetterFileService.deleteFile(userId, id);
        return ResponseEntity.ok().build();
    }
}