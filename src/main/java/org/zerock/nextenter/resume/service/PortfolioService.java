package org.zerock.nextenter.resume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.resume.dto.*;
import org.zerock.nextenter.resume.entity.Portfolio;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.PortfolioRepository;
import org.zerock.nextenter.resume.repository.ResumeRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 포트폴리오 서비스
 * - 파일 업로드, 삭제, 조회 등의 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ResumeRepository resumeRepository;

    @Value("${file.portfolio-upload-dir:uploads/portfolios}")
    private String uploadDir;

    // 파일 크기 제한: 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "doc", "hwp", "pptx", "ppt", "xlsx", "xls", "zip", "jpg", "jpeg", "png"
    );

    // 이력서당 최대 포트폴리오 파일 개수
    private static final int MAX_PORTFOLIO_COUNT = 10;

    /**
     * 포트폴리오 파일 업로드
     * @param userId 사용자 ID
     * @param resumeId 이력서 ID
     * @param file 업로드할 파일
     * @param description 포트폴리오 설명
     * @return 업로드 응답 DTO
     */
    @Transactional
    public PortfolioUploadResponse uploadPortfolio(Long userId, Long resumeId, 
                                                    MultipartFile file, String description) {
        // 1. 이력서 존재 및 소유권 확인
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (!resume.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 이력서에만 포트폴리오를 추가할 수 있습니다");
        }

        // 2. 포트폴리오 개수 제한 확인
        Long currentCount = portfolioRepository.countByResumeId(resumeId);
        if (currentCount >= MAX_PORTFOLIO_COUNT) {
            throw new IllegalArgumentException("이력서당 최대 " + MAX_PORTFOLIO_COUNT + "개의 포트폴리오만 첨부할 수 있습니다");
        }

        // 3. 파일 검증
        validateFile(file);

        // 4. 파일 저장
        String originalFileName = file.getOriginalFilename();
        String savedFileName = generateFileName(file);
        String filePath = saveFile(file, savedFileName);
        String fileType = getFileExtension(file);

        // 5. Portfolio 엔티티 생성 및 저장
        Portfolio portfolio = Portfolio.builder()
                .resume(resume)
                .fileName(originalFileName)
                .filePath(filePath)
                .fileType(fileType)
                .fileSize(file.getSize())
                .description(description)
                .displayOrder(currentCount.intValue()) // 현재 개수를 순서로 설정
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        log.info("포트폴리오 업로드 성공 - resumeId: {}, portfolioId: {}, fileName: {}", 
                 resumeId, savedPortfolio.getPortfolioId(), originalFileName);

        // 6. 응답 생성
        return PortfolioUploadResponse.success(
                savedPortfolio.getPortfolioId(),
                resumeId,
                originalFileName,
                fileType,
                file.getSize()
        );
    }

    /**
     * 특정 이력서의 모든 포트폴리오 조회
     * @param userId 사용자 ID
     * @param resumeId 이력서 ID
     * @return 포트폴리오 목록 응답
     */
    @Transactional(readOnly = true)
    public PortfolioListResponse getPortfoliosByResumeId(Long userId, Long resumeId) {
        // 1. 이력서 존재 및 소유권 확인
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (!resume.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 이력서만 조회할 수 있습니다");
        }

        // 2. 포트폴리오 목록 조회
        List<Portfolio> portfolios = portfolioRepository.findByResumeIdOrderByDisplayOrder(resumeId);

        // 3. DTO 변환
        List<PortfolioDto> portfolioDtos = portfolios.stream()
                .map(PortfolioDto::fromEntity)
                .collect(Collectors.toList());

        // 4. 전체 파일 크기 계산
        Long totalFileSize = portfolioRepository.sumFileSizeByResumeId(resumeId);
        if (totalFileSize == null) totalFileSize = 0L;

        return PortfolioListResponse.builder()
                .resumeId(resumeId)
                .totalCount((long) portfolios.size())
                .totalFileSize(totalFileSize)
                .portfolios(portfolioDtos)
                .build();
    }

    /**
     * 포트폴리오 상세 조회
     * @param userId 사용자 ID
     * @param resumeId 이력서 ID
     * @param portfolioId 포트폴리오 ID
     * @return 포트폴리오 DTO
     */
    @Transactional(readOnly = true)
    public PortfolioDto getPortfolio(Long userId, Long resumeId, Long portfolioId) {
        Portfolio portfolio = findPortfolioWithValidation(userId, resumeId, portfolioId);
        return PortfolioDto.fromEntity(portfolio);
    }

    /**
     * 포트폴리오 수정 (설명, 표시 순서)
     * @param userId 사용자 ID
     * @param resumeId 이력서 ID
     * @param portfolioId 포트폴리오 ID
     * @param request 수정 요청 DTO
     * @return 수정된 포트폴리오 DTO
     */
    @Transactional
    public PortfolioDto updatePortfolio(Long userId, Long resumeId, Long portfolioId, 
                                        PortfolioUpdateRequest request) {
        Portfolio portfolio = findPortfolioWithValidation(userId, resumeId, portfolioId);

        // 설명 업데이트
        if (request.getDescription() != null) {
            portfolio.updateDescription(request.getDescription());
        }

        // 표시 순서 업데이트
        if (request.getDisplayOrder() != null) {
            portfolio.updateDisplayOrder(request.getDisplayOrder());
        }

        Portfolio updated = portfolioRepository.save(portfolio);

        log.info("포트폴리오 수정 성공 - portfolioId: {}", portfolioId);

        return PortfolioDto.fromEntity(updated);
    }

    /**
     * 포트폴리오 삭제
     * @param userId 사용자 ID
     * @param resumeId 이력서 ID
     * @param portfolioId 포트폴리오 ID
     */
    @Transactional
    public void deletePortfolio(Long userId, Long resumeId, Long portfolioId) {
        Portfolio portfolio = findPortfolioWithValidation(userId, resumeId, portfolioId);

        // 1. 물리적 파일 삭제
        deletePhysicalFile(portfolio.getFilePath());

        // 2. DB 레코드 삭제
        portfolioRepository.delete(portfolio);

        log.info("포트폴리오 삭제 성공 - portfolioId: {}, fileName: {}", 
                 portfolioId, portfolio.getFileName());
    }

    /**
     * 포트폴리오 파일 다운로드
     * @param userId 사용자 ID
     * @param resumeId 이력서 ID
     * @param portfolioId 포트폴리오 ID
     * @return 파일 리소스
     */
    @Transactional(readOnly = true)
    public Resource downloadPortfolio(Long userId, Long resumeId, Long portfolioId) {
        Portfolio portfolio = findPortfolioWithValidation(userId, resumeId, portfolioId);

        try {
            Path filePath = Paths.get(portfolio.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("파일을 읽을 수 없습니다");
            }
        } catch (Exception e) {
            log.error("파일 다운로드 실패 - portfolioId: {}", portfolioId, e);
            throw new RuntimeException("파일 다운로드 실패", e);
        }
    }

    // ==================== Private 메서드 ====================

    /**
     * 포트폴리오 조회 및 권한 검증
     */
    private Portfolio findPortfolioWithValidation(Long userId, Long resumeId, Long portfolioId) {
        // 1. 포트폴리오 조회
        Portfolio portfolio = portfolioRepository.findByIdAndResumeId(portfolioId, resumeId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다"));

        // 2. 소유권 확인
        if (!portfolio.getResume().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 포트폴리오만 접근할 수 있습니다");
        }

        return portfolio;
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 50MB를 초과할 수 없습니다");
        }

        String extension = getFileExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "허용되지 않는 파일 형식입니다. 허용 형식: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
    }

    /**
     * 파일 저장
     */
    private String saveFile(MultipartFile file, String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    /**
     * 물리적 파일 삭제
     */
    private void deletePhysicalFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", filePath, e);
        }
    }

    /**
     * UUID를 사용한 고유 파일명 생성
     */
    private String generateFileName(MultipartFile file) {
        String extension = getFileExtension(file);
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 없습니다");
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
}
