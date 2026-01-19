package org.zerock.nextenter.advertisement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.advertisement.dto.AdvertisementDto;
import org.zerock.nextenter.advertisement.dto.AdvertisementRequest;
import org.zerock.nextenter.advertisement.service.AdvertisementService;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Advertisement", description = "광고 관리 API")
@CrossOrigin(origins = "*")
public class AdvertisementController {
    
    private final AdvertisementService advertisementService;
    
    @Operation(summary = "활성화된 광고 목록 조회", description = "현재 활성화된 모든 광고를 우선순위 순으로 조회합니다")
    @GetMapping("/active")
    public ResponseEntity<List<AdvertisementDto>> getActiveAdvertisements() {
        log.info("GET /api/advertisements/active");
        List<AdvertisementDto> advertisements = advertisementService.getActiveAdvertisements();
        return ResponseEntity.ok(advertisements);
    }
    
    @Operation(summary = "기업의 광고 목록 조회", description = "특정 기업이 등록한 모든 광고를 조회합니다")
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<AdvertisementDto>> getCompanyAdvertisements(
            @PathVariable Long companyId) {
        log.info("GET /api/advertisements/company/{}", companyId);
        List<AdvertisementDto> advertisements = advertisementService.getAdvertisementsByCompany(companyId);
        return ResponseEntity.ok(advertisements);
    }
    
    @Operation(summary = "광고 생성", description = "새로운 광고를 생성합니다 (기업 전용)")
    @PostMapping("/company/{companyId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<AdvertisementDto> createAdvertisement(
            @PathVariable Long companyId,
            @RequestBody AdvertisementRequest request) {
        log.info("POST /api/advertisements/company/{} - {}", companyId, request);
        AdvertisementDto created = advertisementService.createAdvertisement(companyId, request);
        return ResponseEntity.ok(created);
    }
    
    @Operation(summary = "광고 수정", description = "기존 광고를 수정합니다")
    @PutMapping("/{advertisementId}/company/{companyId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<AdvertisementDto> updateAdvertisement(
            @PathVariable Long advertisementId,
            @PathVariable Long companyId,
            @RequestBody AdvertisementRequest request) {
        log.info("PUT /api/advertisements/{}/company/{} - {}", advertisementId, companyId, request);
        AdvertisementDto updated = advertisementService.updateAdvertisement(advertisementId, companyId, request);
        return ResponseEntity.ok(updated);
    }
    
    @Operation(summary = "광고 활성화/비활성화", description = "광고의 활성화 상태를 토글합니다")
    @PatchMapping("/{advertisementId}/company/{companyId}/toggle")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> toggleAdvertisementStatus(
            @PathVariable Long advertisementId,
            @PathVariable Long companyId) {
        log.info("PATCH /api/advertisements/{}/company/{}/toggle", advertisementId, companyId);
        advertisementService.toggleAdvertisementStatus(advertisementId, companyId);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "광고 삭제", description = "광고를 삭제합니다")
    @DeleteMapping("/{advertisementId}/company/{companyId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> deleteAdvertisement(
            @PathVariable Long advertisementId,
            @PathVariable Long companyId) {
        log.info("DELETE /api/advertisements/{}/company/{}", advertisementId, companyId);
        advertisementService.deleteAdvertisement(advertisementId, companyId);
        return ResponseEntity.ok().build();
    }
}
