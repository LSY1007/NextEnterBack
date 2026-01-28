package org.zerock.nextenter.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.application.dto.ApplicationSummaryResponse;
import org.zerock.nextenter.application.service.ApplicationIntegrationService;

import java.util.List;

@Tag(name = "Application Integration", description = "통합 지원 관리 API")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationIntegrationService integrationService;

    @Operation(
        summary = "내 모든 지원 내역 조회 (통합)", 
        description = "일반 지원과 면접 제안을 모두 포함한 통합 지원 내역 조회. 기존 GET /api/applies/my를 대체합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<List<ApplicationSummaryResponse>> getMyApplications(
            @Parameter(description = "사용자 ID", required = true)
            @RequestHeader("userId") Long userId
    ) {
        log.info("GET /api/applications/my - userId: {}", userId);

        List<ApplicationSummaryResponse> applications = 
            integrationService.getMyApplications(userId);

        return ResponseEntity.ok(applications);
    }
}
