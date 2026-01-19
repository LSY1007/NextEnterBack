package org.zerock.nextenter.advertisement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.advertisement.dto.AdvertisementDto;
import org.zerock.nextenter.advertisement.dto.AdvertisementRequest;
import org.zerock.nextenter.advertisement.entity.Advertisement;
import org.zerock.nextenter.advertisement.repository.AdvertisementRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AdvertisementService {
    
    private final AdvertisementRepository advertisementRepository;
    
    /**
     * 활성화된 모든 광고 조회 (우선순위 순)
     */
    public List<AdvertisementDto> getActiveAdvertisements() {
        return advertisementRepository.findByIsActiveTrueOrderByPriorityDescCreatedAtDesc()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 기업의 광고 조회
     */
    public List<AdvertisementDto> getAdvertisementsByCompany(Long companyId) {
        return advertisementRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 광고 생성 (기업 전용)
     */
    public AdvertisementDto createAdvertisement(Long companyId, AdvertisementRequest request) {
        Advertisement advertisement = Advertisement.builder()
                .companyId(companyId)
                .title(request.getTitle())
                .description(request.getDescription())
                .backgroundColor(request.getBackgroundColor())
                .buttonText(request.getButtonText())
                .targetUrl(request.getTargetUrl())
                .targetPage(request.getTargetPage())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(true)
                .build();
        
        Advertisement saved = advertisementRepository.save(advertisement);
        log.info("Created advertisement: {}", saved.getId());
        
        return entityToDto(saved);
    }
    
    /**
     * 광고 수정
     */
    public AdvertisementDto updateAdvertisement(Long advertisementId, Long companyId, AdvertisementRequest request) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
        
        // 해당 기업의 광고인지 확인
        if (!advertisement.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Unauthorized access to advertisement");
        }
        
        // 수정 가능한 필드 업데이트
        Advertisement updated = Advertisement.builder()
                .id(advertisement.getId())
                .companyId(advertisement.getCompanyId())
                .title(request.getTitle() != null ? request.getTitle() : advertisement.getTitle())
                .description(request.getDescription() != null ? request.getDescription() : advertisement.getDescription())
                .backgroundColor(request.getBackgroundColor() != null ? request.getBackgroundColor() : advertisement.getBackgroundColor())
                .buttonText(request.getButtonText() != null ? request.getButtonText() : advertisement.getButtonText())
                .targetUrl(request.getTargetUrl() != null ? request.getTargetUrl() : advertisement.getTargetUrl())
                .targetPage(request.getTargetPage() != null ? request.getTargetPage() : advertisement.getTargetPage())
                .priority(request.getPriority() != null ? request.getPriority() : advertisement.getPriority())
                .isActive(advertisement.getIsActive())
                .createdAt(advertisement.getCreatedAt())
                .build();
        
        Advertisement saved = advertisementRepository.save(updated);
        log.info("Updated advertisement: {}", saved.getId());
        
        return entityToDto(saved);
    }
    
    /**
     * 광고 활성화/비활성화
     */
    public void toggleAdvertisementStatus(Long advertisementId, Long companyId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
        
        if (!advertisement.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Unauthorized access to advertisement");
        }
        
        advertisement.changeActiveStatus(!advertisement.getIsActive());
        advertisementRepository.save(advertisement);
        log.info("Toggled advertisement {} status to: {}", advertisementId, advertisement.getIsActive());
    }
    
    /**
     * 광고 삭제
     */
    public void deleteAdvertisement(Long advertisementId, Long companyId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found: " + advertisementId));
        
        if (!advertisement.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Unauthorized access to advertisement");
        }
        
        advertisementRepository.delete(advertisement);
        log.info("Deleted advertisement: {}", advertisementId);
    }
    
    // Entity to DTO 변환
    private AdvertisementDto entityToDto(Advertisement advertisement) {
        return AdvertisementDto.builder()
                .id(advertisement.getId())
                .companyId(advertisement.getCompanyId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .backgroundColor(advertisement.getBackgroundColor())
                .buttonText(advertisement.getButtonText())
                .targetUrl(advertisement.getTargetUrl())
                .targetPage(advertisement.getTargetPage())
                .isActive(advertisement.getIsActive())
                .priority(advertisement.getPriority())
                .build();
    }
}
