package org.zerock.nextenter.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.coverletter.entity.CoverLetter;
import org.zerock.nextenter.coverletter.repository.CoverLetterRepository;
import org.zerock.nextenter.resume.dto.ResumeListResponse;
import org.zerock.nextenter.resume.dto.ResumeRequest;
import org.zerock.nextenter.resume.dto.ResumeResponse;
import org.zerock.nextenter.resume.dto.TalentSearchResponse;
import org.zerock.nextenter.resume.entity.Portfolio;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.entity.TalentContact;
import org.zerock.nextenter.resume.repository.PortfolioRepository;
import org.zerock.nextenter.resume.repository.ResumeRepository;
import org.zerock.nextenter.resume.repository.TalentContactRepository;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final TalentContactRepository talentContactRepository;
    private final PortfolioRepository portfolioRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 이력서 CRUD ====================

    /**
     * 이력서 목록 조회
     */
    public List<ResumeListResponse> getResumeList(Long userId) {
        log.info("이력서 목록 조회 - userId: {}", userId);

        List<Resume> resumes = resumeRepository
                .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);

        return resumes.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 이력서 상세 조회
     */
    @Transactional
    public ResumeResponse getResumeDetail(Long resumeId, Long userId) {
        log.info("이력서 상세 조회 - resumeId: {}, userId: {}", resumeId, userId);

        Resume resume = resumeRepository
                .findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다"));

        // 조회수 증가
        resumeRepository.incrementViewCount(resumeId);

        return convertToResponse(resume);
    }

    /**
     * 공개 이력서 조회 (기업회원용)
     */
    @Transactional
    public ResumeResponse getPublicResumeDetail(Long resumeId) {
        log.info("공개 이력서 조회 - resumeId: {}", resumeId);

        Resume resume = resumeRepository
                .findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없습니다"));

        // 삭제된 이력서 확인
        if (resume.getDeletedAt() != null) {
            throw new IllegalArgumentException("삭제된 이력서입니다");
        }

        // 공개 여부 확인
        if (resume.getVisibility() != Resume.Visibility.PUBLIC) {
            throw new IllegalArgumentException("비공개 이력서는 조회할 수 없습니다");
        }

        // 조회수 증가
        resumeRepository.incrementViewCount(resumeId);

        return convertToResponse(resume);
    }

    /**
     * 이력서 파일 업로드 (AI 처리는 나중에)
     */
    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, Long userId) {
        log.info("이력서 파일 업로드 - userId: {}, filename: {}", userId, file.getOriginalFilename());

        try {
            // 파일 검증 및 저장
            fileStorageService.validateFile(file);
            String filename = fileStorageService.saveFile(file);
            String filePath = fileStorageService.getFileUrl(filename);
            String fileType = getFileExtension(file.getOriginalFilename());

            // DB에 저장 (AI 처리는 나중에)
            Resume resume = Resume.builder()
                    .userId(userId)
                    .title(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileType(fileType)
                    .status("DRAFT")  // AI 처리 전이므로 DRAFT
                    .build();

            resume = resumeRepository.save(resume);
            log.info("이력서 업로드 완료 - resumeId: {}", resume.getResumeId());

            return convertToResponse(resume);

        } catch (Exception e) {
            log.error("이력서 업로드 실패", e);
            throw new RuntimeException("이력서 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이력서 생성
     */
    @Transactional
    public ResumeResponse createResume(ResumeRequest request, Long userId) {
        log.info("이력서 생성 - userId: {}, title: {}", userId, request.getTitle());

        // visibility 처리
        Resume.Visibility visibility = Resume.Visibility.PUBLIC;
        if (request.getVisibility() != null) {
            try {
                visibility = Resume.Visibility.valueOf(request.getVisibility().toUpperCase());
                log.info("설정된 visibility: {}", visibility);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 visibility 값: {}, 기본값 PUBLIC 사용", request.getVisibility());
            }
        }

        Resume resume = Resume.builder()
                .userId(userId)
                .title(request.getTitle())
                .jobCategory(request.getJobCategory())
                // ===== 분리된 섹션들 저장 =====
                .experiences(request.getExperiences())
                .certificates(request.getCertificates())
                .educations(request.getEducations())
                .careers(request.getCareers())
                // ===== 기존 필드들 =====
                .skills(request.getSkills())
                .visibility(visibility)
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .build();

        // 하위 호환성: sections가 있으면 structuredData에 저장
        if (request.getSections() != null && !request.getSections().isEmpty()) {
            resume.setStructuredData(request.getSections());
        }

        resume = resumeRepository.save(resume);
        log.info("이력서 생성 완료 - resumeId: {}", resume.getResumeId());

        return convertToResponse(resume);
    }

    /**
     * 이력서 수정
     */
    @Transactional
    public ResumeResponse updateResume(Long resumeId, ResumeRequest request, Long userId) {
        log.info("이력서 수정 - resumeId: {}, userId: {}", resumeId, userId);

        Resume resume = resumeRepository
                .findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다"));

        // 기본 정보 업데이트
        if (request.getTitle() != null) {
            resume.setTitle(request.getTitle());
        }
        if (request.getJobCategory() != null) {
            resume.setJobCategory(request.getJobCategory());
        }
        if (request.getStatus() != null) {
            resume.setStatus(request.getStatus());
        }

        // ===== 분리된 섹션들 업데이트 =====
        if (request.getExperiences() != null) {
            resume.setExperiences(request.getExperiences());
        }
        if (request.getCertificates() != null) {
            resume.setCertificates(request.getCertificates());
        }
        if (request.getEducations() != null) {
            resume.setEducations(request.getEducations());
        }
        if (request.getCareers() != null) {
            resume.setCareers(request.getCareers());
        }

        // ===== 기존 필드들 업데이트 =====
        if (request.getSkills() != null) {
            resume.setSkills(request.getSkills());
        }

        // visibility 업데이트
        if (request.getVisibility() != null) {
            try {
                Resume.Visibility visibility = Resume.Visibility.valueOf(request.getVisibility().toUpperCase());
                resume.setVisibility(visibility);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 visibility 값: {}, 업데이트 건너뛴", request.getVisibility());
            }
        }

        // 하위 호환성: sections가 있으면 structuredData에 저장
        if (request.getSections() != null && !request.getSections().isEmpty()) {
            resume.setStructuredData(request.getSections());
        }

        resume = resumeRepository.save(resume);
        log.info("이력서 수정 완료 - resumeId: {}", resume.getResumeId());

        return convertToResponse(resume);
    }

    /**
     * 이력서 삭제
     */
    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        log.info("이력서 삭제 - resumeId: {}, userId: {}", resumeId, userId);

        Resume resume = resumeRepository
                .findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다"));

        // ✅ 1. 연결된 포트폴리오 먼저 삭제
        List<Portfolio> portfolios = portfolioRepository.findByResumeIdOrderByDisplayOrder(resumeId);
        for (Portfolio portfolio : portfolios) {
            try {
                // 포트폴리오 파일 삭제
                if (portfolio.getFilePath() != null) {
                    String filename = portfolio.getFilePath().substring(
                            portfolio.getFilePath().lastIndexOf("/") + 1);
                    fileStorageService.deleteFile(filename);
                }
                // DB에서 포트폴리오 삭제
                portfolioRepository.delete(portfolio);
                log.info("포트폴리오 삭제 완료 - portfolioId: {}", portfolio.getPortfolioId());
            } catch (Exception e) {
                log.error("포트폴리오 삭제 실패 - portfolioId: {}", portfolio.getPortfolioId(), e);
            }
        }

        // ✅ 2. 이력서 파일 삭제
        if (resume.getFilePath() != null) {
            try {
                String filename = resume.getFilePath().substring(
                        resume.getFilePath().lastIndexOf("/") + 1);
                fileStorageService.deleteFile(filename);
            } catch (Exception e) {
                log.error("파일 삭제 실패: {}", resume.getFilePath(), e);
            }
        }

        // ✅ 3. 이력서 삭제
        resumeRepository.delete(resume);
        log.info("이력서 물리적 삭제 완료 - resumeId: {}", resumeId);
    }

    /**
     * 이력서 파일 다운로드
     */
    public Resource downloadResumeFile(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (resume.getFilePath() == null || resume.getFilePath().isEmpty()) {
            throw new IllegalArgumentException("다운로드할 파일이 없습니다");
        }

        try {
            Path filePath = Paths.get(resume.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("파일을 읽을 수 없습니다");
            }
        } catch (Exception e) {
            log.error("이력서 파일 다운로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 다운로드에 실패했습니다", e);
        }
    }

    // ==================== 인재 검색 ====================

    /**
     * 인재 검색
     */
    public Page<TalentSearchResponse> searchTalents(
            String jobCategory, String keyword, int page, int size, Long companyUserId) {

        log.info("인재 검색 - jobCategory: {}, keyword: {}, page: {}, companyUserId: {}",
                jobCategory, keyword, page, companyUserId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Resume> resumePage = resumeRepository.searchTalents(jobCategory, keyword, pageable);

        return resumePage.map(resume -> {
            // User 정보 조회
            User user = userRepository.findById(resume.getUserId()).orElse(null);

            // 이름 추출 (User 테이블에서)
            String realName = (user != null && user.getName() != null) ? user.getName() : "익명";
            String maskedName = maskName(realName);

            // 기술 스택 파싱
            List<String> skillsList = parseSkills(resume.getSkills());

            // 경력 계산 (careers JSON에서)
            int experienceYears = calculateExperienceYearsFromJson(resume.getCareers());

            // 매칭 점수 계산
            int matchScore = 80 + (int)(Math.random() * 16);

            // 연락 상태 확인
            String contactStatus = null;
            if (companyUserId != null) {
                List<TalentContact> contacts = talentContactRepository
                        .findByResumeIdOrderByCreatedAtDesc(resume.getResumeId());

                for (TalentContact contact : contacts) {
                    if (contact.getCompanyUserId().equals(companyUserId)) {
                        contactStatus = contact.getStatus();
                        break;
                    }
                }
            }

            return TalentSearchResponse.builder()
                    .resumeId(resume.getResumeId())
                    .userId(resume.getUserId())
                    .name(maskedName)
                    .jobCategory(resume.getJobCategory())
                    .skills(skillsList)
                    .location("미지정") // 필요시 User 테이블에 추가
                    .experienceYears(experienceYears)
                    .salaryRange("협의")
                    .matchScore(matchScore)
                    .isAvailable(true)
                    .viewCount(resume.getViewCount())
                    .contactStatus(contactStatus)
                    .build();
        });
    }

    // ==================== Private Methods ====================

    /**
     * ResumeListResponse 변환
     */
    private ResumeListResponse convertToListResponse(Resume resume) {
        return ResumeListResponse.builder()
                .resumeId(resume.getResumeId())
                .title(resume.getTitle())
                .jobCategory(resume.getJobCategory())
                .isMain(resume.getIsMain())
                .visibility(resume.getVisibility().name())
                .viewCount(resume.getViewCount())
                .status(resume.getStatus())
                .createdAt(resume.getCreatedAt())
                .build();
    }

    /**
     * ResumeResponse 변환 (User 정보 포함)
     */
    private ResumeResponse convertToResponse(Resume resume) {
        // User 정보 조회
        User user = userRepository.findById(resume.getUserId()).orElse(null);

        // ✅ 포트폴리오 목록 조회
        List<Portfolio> portfolios = portfolioRepository.findByResumeIdOrderByDisplayOrder(resume.getResumeId());
        
        // ✅ 자기소개서 목록 조회 (이 이력서의 userId와 같은 자기소개서들)
        List<CoverLetter> coverLetters = coverLetterRepository.findByUserIdOrderByCreatedAtDesc(resume.getUserId());
        
        ResumeResponse.ResumeResponseBuilder builder = ResumeResponse.builder()
                .resumeId(resume.getResumeId())
                .title(resume.getTitle())
                .jobCategory(resume.getJobCategory())
                // ===== User 테이블에서 가져온 정보 =====
                .userName(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userGender(user != null && user.getGender() != null ? user.getGender().name() : null)
                .userPhone(user != null ? user.getPhone() : null)
                .userAge(user != null ? user.getAge() : null)
                .userBio(user != null ? user.getBio() : null)
                // ===== 분리된 섹션들 =====
                .experiences(resume.getExperiences())
                .certificates(resume.getCertificates())
                .educations(resume.getEducations())
                .careers(resume.getCareers())
                // ===== 기존 필드들 =====
                .extractedText(resume.getExtractedText())
                .skills(resume.getSkills())
                .resumeRecommend(resume.getResumeRecommend())
                .filePath(resume.getFilePath())
                .fileType(resume.getFileType())
                .isMain(resume.getIsMain())
                .visibility(resume.getVisibility().name())
                .viewCount(resume.getViewCount())
                .status(resume.getStatus())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt());

        // ✅ 포트폴리오 리스트 추가
        if (!portfolios.isEmpty()) {
            List<ResumeResponse.PortfolioInfo> portfolioInfoList = portfolios.stream()
                    .map(p -> ResumeResponse.PortfolioInfo.builder()
                            .portfolioId(p.getPortfolioId())
                            .filename(p.getFileName())
                            .filePath(p.getFilePath())
                            .fileType(p.getFileType())
                            .fileSize(p.getFileSize())
                            .description(p.getDescription())
                            .displayOrder(p.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
            builder.portfolios(portfolioInfoList);
        }

        // ✅ 자기소개서 리스트 추가
        if (!coverLetters.isEmpty()) {
            List<ResumeResponse.CoverLetterInfo> coverLetterInfoList = coverLetters.stream()
                    .map(c -> ResumeResponse.CoverLetterInfo.builder()
                            .coverLetterId(c.getCoverLetterId())
                            .title(c.getTitle())
                            .content(c.getContent())
                            .filePath(c.getFilePath())
                            .fileType(c.getFileType())
                            .build())
                    .collect(Collectors.toList());
            builder.coverLetters(coverLetterInfoList);
        }

        // 하위 호환성: structuredData 포함
        if (resume.getStructuredData() != null) {
            builder.structuredData(resume.getStructuredData());
        }

        return builder.build();
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 이름 마스킹
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return "익명";
        }

        // 한글: 성만 표시 (예: "김철수" -> "김**")
        if (isKorean(name.charAt(0))) {
            return name.charAt(0) + "**";
        }

        // 영문: 첫 글자만 표시 (예: "John" -> "J**")
        return name.charAt(0) + "**";
    }

    private boolean isKorean(char c) {
        return (c >= '\uac00' && c <= '\ud7a3');
    }

    /**
     * 기술 스택 파싱
     */
    private List<String> parseSkills(String skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 경력 JSON에서 경력 년수 계산
     */
    private int calculateExperienceYearsFromJson(String careersJson) {
        if (careersJson == null || careersJson.isEmpty()) {
            return 0;
        }

        try {
            JsonNode careersArray = objectMapper.readTree(careersJson);

            if (careersArray.isArray() && careersArray.size() > 0) {
                int totalMonths = 0;

                for (JsonNode career : careersArray) {
                    if (career.has("period")) {
                        String period = career.get("period").asText();
                        totalMonths += parsePeriodToMonths(period);
                    }
                }

                return totalMonths / 12;
            }
        } catch (Exception e) {
            log.warn("경력 JSON 파싱 실패: {}", e.getMessage());
        }

        return 0;
    }

    /**
     * 기간 문자열을 개월수로 변환
     */
    private int parsePeriodToMonths(String period) {
        try {
            String[] parts = period.split("~");
            if (parts.length != 2) return 0;

            String start = parts[0].trim().replace(" ", "");
            String end = parts[1].trim().replace(" ", "");

            String[] startParts = start.split("\\.");
            String[] endParts = end.split("\\.");

            if (startParts.length >= 2 && endParts.length >= 2) {
                int startYear = Integer.parseInt(startParts[0]);
                int startMonth = Integer.parseInt(startParts[1]);
                int endYear = Integer.parseInt(endParts[0]);
                int endMonth = Integer.parseInt(endParts[1]);

                return (endYear - startYear) * 12 + (endMonth - startMonth);
            }
        } catch (Exception e) {
            log.warn("기간 파싱 실패: {}", period);
        }
        return 0;
    }

    /**
     * 파일 포함 이력서 생성
     */
    @Transactional
    public ResumeResponse createResumeWithFiles(
            ResumeRequest request,
            Long userId,
            List<MultipartFile> portfolioFiles,
            List<MultipartFile> coverLetterFiles) {

        log.info("파일 포함 이력서 생성 - userId: {}, title: {}", userId, request.getTitle());

        // 1. 기본 이력서 생성
        ResumeResponse resume = createResume(request, userId);

        // 2. 포트폴리오 파일 저장
        if (portfolioFiles != null && !portfolioFiles.isEmpty()) {
            int displayOrder = 0;
            for (MultipartFile file : portfolioFiles) {
                try {
                    String filename = fileStorageService.saveFile(file);
                    String filePath = fileStorageService.getFileUrl(filename);
                    String fileType = getFileExtension(file.getOriginalFilename());

                    Resume resumeEntity = resumeRepository.findById(resume.getResumeId())
                            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

                    Portfolio portfolio = Portfolio.builder()
                            .resume(resumeEntity)
                            .fileName(file.getOriginalFilename())
                            .filePath(filePath)
                            .fileType(fileType)
                            .fileSize(file.getSize())
                            .displayOrder(displayOrder++)
                            .build();

                    portfolioRepository.save(portfolio);
                } catch (Exception e) {
                    log.error("포트폴리오 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 3. 자기소개서 파일 저장
        if (coverLetterFiles != null && !coverLetterFiles.isEmpty()) {
            for (MultipartFile file : coverLetterFiles) {
                try {
                    String filename = fileStorageService.saveFile(file);
                    String filePath = fileStorageService.getFileUrl(filename);
                    String fileType = getFileExtension(file.getOriginalFilename());

                    CoverLetter coverLetter = CoverLetter.builder()
                            .userId(userId)
                            .title(file.getOriginalFilename())
                            .filePath(filePath)
                            .fileType(fileType)
                            .build();

                    coverLetterRepository.save(coverLetter);
                } catch (Exception e) {
                    log.error("자기소개서 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 4. 업데이트된 이력서 다시 조회
        Resume updatedResume = resumeRepository.findById(resume.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        return convertToResponse(updatedResume);
    }

    /**
     * 파일 포함 이력서 수정
     */
    @Transactional
    public ResumeResponse updateResumeWithFiles(
            Long resumeId,
            ResumeRequest request,
            Long userId,
            List<MultipartFile> portfolioFiles,
            List<MultipartFile> coverLetterFiles) {

        log.info("파일 포함 이력서 수정 - resumeId: {}, userId: {}", resumeId, userId);

        // 1. 기본 정보 업데이트
        ResumeResponse resume = updateResume(resumeId, request, userId);

        // 2. 새 포트폴리오 파일 추가
        if (portfolioFiles != null && !portfolioFiles.isEmpty()) {
            List<Portfolio> existingPortfolios = portfolioRepository.findByResumeIdOrderByDisplayOrder(resumeId);
            int displayOrder = existingPortfolios.size();

            for (MultipartFile file : portfolioFiles) {
                try {
                    String filename = fileStorageService.saveFile(file);
                    String filePath = fileStorageService.getFileUrl(filename);
                    String fileType = getFileExtension(file.getOriginalFilename());

                    Resume resumeEntity = resumeRepository.findById(resumeId)
                            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

                    Portfolio portfolio = Portfolio.builder()
                            .resume(resumeEntity)
                            .fileName(file.getOriginalFilename())
                            .filePath(filePath)
                            .fileType(fileType)
                            .fileSize(file.getSize())
                            .displayOrder(displayOrder++)
                            .build();

                    portfolioRepository.save(portfolio);
                } catch (Exception e) {
                    log.error("포트폴리오 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 3. 새 자기소개서 파일 추가
        if (coverLetterFiles != null && !coverLetterFiles.isEmpty()) {
            for (MultipartFile file : coverLetterFiles) {
                try {
                    String filename = fileStorageService.saveFile(file);
                    String filePath = fileStorageService.getFileUrl(filename);
                    String fileType = getFileExtension(file.getOriginalFilename());

                    CoverLetter coverLetter = CoverLetter.builder()
                            .userId(userId)
                            .title(file.getOriginalFilename())
                            .filePath(filePath)
                            .fileType(fileType)
                            .build();

                    coverLetterRepository.save(coverLetter);
                } catch (Exception e) {
                    log.error("자기소개서 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 4. 업데이트된 이력서 다시 조회
        Resume updatedResume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        return convertToResponse(updatedResume);
    }
}