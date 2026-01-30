package org.zerock.nextenter.resume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.resume.dto.PortfolioUploadResponse;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;

/**
 * 포트폴리오 전용 서비스
 * - Resume 없이 Portfolio만 업로드하는 기능
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StandalonePortfolioService {

    private final ResumeRepository resumeRepository;
    private final PortfolioService portfolioService;

    /**
     * 포트폴리오만 업로드 (임시 Resume 자동 생성)
     * 
     * @param userId      사용자 ID
     * @param file        포트폴리오 파일
     * @param description 포트폴리오 설명
     * @return 포트폴리오 업로드 응답
     */
    @Transactional
    public PortfolioUploadResponse uploadPortfolioOnly(Long userId, MultipartFile file, String description) {
        log.info("========================================");
        log.info("📦 [STANDALONE-PORTFOLIO] 포트폴리오 단독 업로드 시작");
        log.info("📦 [STANDALONE-PORTFOLIO] userId: {}, filename: {}", userId, file.getOriginalFilename());
        log.info("========================================");

        // 1. 임시 Resume 자동 생성
        Resume tempResume = Resume.builder()
                .userId(userId)
                .title("포트폴리오 첨부용 임시 이력서")
                .status("DRAFT")
                .visibility(Resume.Visibility.PRIVATE) // 비공개로 설정
                .build();

        Resume savedResume = resumeRepository.save(tempResume);
        log.info("📦 [STANDALONE-PORTFOLIO] 임시 Resume 생성 완료 - resumeId: {}", savedResume.getResumeId());

        // 2. Portfolio 업로드 (기존 PortfolioService 활용)
        PortfolioUploadResponse response = portfolioService.uploadPortfolio(
                userId, savedResume.getResumeId(), file, description);

        log.info("✅ [STANDALONE-PORTFOLIO] 포트폴리오 업로드 성공 - portfolioId: {}, resumeId: {}",
                response.getPortfolioId(), savedResume.getResumeId());

        return response;
    }
}
