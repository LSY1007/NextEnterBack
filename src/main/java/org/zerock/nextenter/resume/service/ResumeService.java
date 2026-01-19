package org.zerock.nextenter.resume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.nextenter.resume.dto.ResumeListResponse;
import org.zerock.nextenter.resume.dto.ResumeRequest;
import org.zerock.nextenter.resume.dto.ResumeResponse;
import org.zerock.nextenter.resume.dto.TalentSearchResponse;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.time.LocalDateTime;
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

    // 이력서 목록 조회
    public List<ResumeListResponse> getResumeList(Long userId) {
        log.info("이력서 목록 조회 - userId: {}", userId);

        List<Resume> resumes = resumeRepository
                .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);

        return resumes.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    // 이력서 상세 조회
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

    // 파일 업로드 (AI 처리는 나중에)
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

    // 이력서 생성 (폼 기반)
    @Transactional
    public ResumeResponse createResume(ResumeRequest request, Long userId) {
        log.info("이력서 생성 - userId: {}, title: {}", userId, request.getTitle());

        Resume resume = Resume.builder()
                .userId(userId)
                .title(request.getTitle())
                .jobCategory(request.getJobCategory())
                .structuredData(request.getSections())
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .build();

        resume = resumeRepository.save(resume);
        log.info("이력서 생성 완료 - resumeId: {}", resume.getResumeId());

        return convertToResponse(resume);
    }

    @Transactional
    public ResumeResponse updateResume(Long resumeId, ResumeRequest request, Long userId) {
        log.info("이력서 수정 - resumeId: {}, userId: {}", resumeId, userId);

        Resume resume = resumeRepository
                .findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다"));

        if (request.getTitle() != null) {
            resume.setTitle(request.getTitle());
        }
        if (request.getJobCategory() != null) {
            resume.setJobCategory(request.getJobCategory());
        }
        if (request.getSections() != null) {
            resume.setStructuredData(request.getSections());
        }
        if (request.getStatus() != null) {
            resume.setStatus(request.getStatus());
        }

        resume = resumeRepository.save(resume);
        log.info("이력서 수정 완료 - resumeId: {}", resume.getResumeId());

        return convertToResponse(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        log.info("이력서 삭제 - resumeId: {}, userId: {}", resumeId, userId);

        Resume resume = resumeRepository
                .findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다"));

        if (resume.getFilePath() != null) {
            try {
                String filename = resume.getFilePath().substring(
                        resume.getFilePath().lastIndexOf("/") + 1);
                fileStorageService.deleteFile(filename);
            } catch (Exception e) {
                log.error("파일 삭제 실패: {}", resume.getFilePath(), e);
            }
        }

        resumeRepository.delete(resume);
        log.info("이력서 물리적 삭제 완료 - resumeId: {}", resumeId);
    }

    /**
     * 인재 검색
     */
    public Page<TalentSearchResponse> searchTalents(
            String jobCategory, String keyword, int page, int size) {
        
        log.info("인재 검색 - jobCategory: {}, keyword: {}, page: {}", jobCategory, keyword, page);

        Pageable pageable = PageRequest.of(page, size);
        Page<Resume> resumePage = resumeRepository.searchTalents(jobCategory, keyword, pageable);

        return resumePage.map(resume -> {
            User user = userRepository.findById(resume.getUserId())
                    .orElse(null);

            // 이름 마스킹 (예: 김철수 -> 김**)
            String maskedName = user != null ? maskName(user.getName()) : "익명";

            // 기술 스택 파싱
            List<String> skillsList = parseSkills(resume.getSkills());

            // 매칭 점수 계산 (임시로 80-95 사이 랜덤)
            int matchScore = 80 + (int)(Math.random() * 16);

            return TalentSearchResponse.builder()
                    .resumeId(resume.getResumeId())
                    .userId(resume.getUserId())
                    .name(maskedName)
                    .jobCategory(resume.getJobCategory())
                    .skills(skillsList)
                    .location("서울") // TODO: User 엔티티에 location 필드 추가 필요
                    .experienceYears(5) // TODO: structuredData에서 파싱 필요
                    .salaryRange("5,000~7,000만원") // TODO: structuredData에서 파싱 필요
                    .matchScore(matchScore)
                    .isAvailable(true) // TODO: User 엔티티에 isAvailable 필드 추가 필요
                    .viewCount(resume.getViewCount())
                    .build();
        });
    }

    // Private Methods
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

    private ResumeResponse convertToResponse(Resume resume) {
        return ResumeResponse.builder()
                .resumeId(resume.getResumeId())
                .title(resume.getTitle())
                .jobCategory(resume.getJobCategory())
                .extractedText(resume.getExtractedText())
                .structuredData(resume.getStructuredData())
                .skills(resume.getSkills())
                .resumeRecommend(resume.getResumeRecommend())
                .filePath(resume.getFilePath())
                .fileType(resume.getFileType())
                .isMain(resume.getIsMain())
                .visibility(resume.getVisibility().name())
                .viewCount(resume.getViewCount())
                .status(resume.getStatus())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return "익명";
        }
        return name.charAt(0) + "**";
    }

    private List<String> parseSkills(String skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        // 쉼표로 구분된 스킬 문자열을 리스트로 변환
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
