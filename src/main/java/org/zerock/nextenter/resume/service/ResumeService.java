package org.zerock.nextenter.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final PortfolioRepository portfolioRepository;  // ✅ 추가
    private final CoverLetterRepository coverLetterRepository;  // ✅ 추가
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용

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

        // ✅ structuredData에 포트폴리오 및 자기소개서 ID 추가
        try {
            updateStructuredDataWithIds(resumeId, userId);
            // resume 객체 다시 조회 (업데이트된 structuredData 반영)
            resume = resumeRepository.findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));
        } catch (Exception e) {
            log.warn("structuredData ID 업데이트 실패 (무시하고 계속): {}", e.getMessage());
        }

        // 조회수 증가
        resumeRepository.incrementViewCount(resumeId);

        return convertToResponse(resume);
    }

    // 공개 이력서 조회 (기업회원용)
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
        log.info("전달받은 skills: {}", request.getSkills());

        // ✅ visibility 처리 추가
        Resume.Visibility visibility = Resume.Visibility.PUBLIC; // ✅ 기본값 PUBLIC
        if (request.getVisibility() != null) {
            try {
                visibility = Resume.Visibility.valueOf(request.getVisibility().toUpperCase());
                log.info("설정된 visibility: {}", visibility);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 visibility 값: {}, 기본값 PRIVATE 사용", request.getVisibility());
            }
        }

        Resume resume = Resume.builder()
                .userId(userId)
                .title(request.getTitle())
                .jobCategory(request.getJobCategory())
                .structuredData(request.getSections())
                .skills(request.getSkills())  // ✅ skills 저장
                .visibility(visibility)  // ✅ visibility 설정
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .build();

        resume = resumeRepository.save(resume);
        log.info("이력서 생성 완료 - resumeId: {}, visibility: {}, skills: {}", 
            resume.getResumeId(), resume.getVisibility(), resume.getSkills());

        return convertToResponse(resume);
    }

    @Transactional
    public ResumeResponse updateResume(Long resumeId, ResumeRequest request, Long userId) {
        log.info("이력서 수정 - resumeId: {}, userId: {}", resumeId, userId);
        log.info("전달받은 skills: {}", request.getSkills());

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
        
        // ✅ skills 업데이트 추가
        if (request.getSkills() != null) {
            resume.setSkills(request.getSkills());
            log.info("업데이트된 skills: {}", request.getSkills());
        }
        
        // ✅ visibility 업데이트 추가
        if (request.getVisibility() != null) {
            try {
                Resume.Visibility visibility = Resume.Visibility.valueOf(request.getVisibility().toUpperCase());
                resume.setVisibility(visibility);
                log.info("업데이트된 visibility: {}", visibility);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 visibility 값: {}, 업데이트 건너뛴", request.getVisibility());
            }
        }

        resume = resumeRepository.save(resume);
        log.info("이력서 수정 완료 - resumeId: {}, visibility: {}, skills: {}", 
            resume.getResumeId(), resume.getVisibility(), resume.getSkills());

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
            String jobCategory, String keyword, int page, int size, Long companyUserId) {
        
        log.info("인재 검색 - jobCategory: {}, keyword: {}, page: {}, companyUserId: {}", 
                jobCategory, keyword, page, companyUserId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Resume> resumePage = resumeRepository.searchTalents(jobCategory, keyword, pageable);

        return resumePage.map(resume -> {
            log.info("파싱 중인 이력서 - resumeId: {}, userId: {}, title: {}", 
                resume.getResumeId(), resume.getUserId(), resume.getTitle());
            log.info("structuredData: {}", resume.getStructuredData());
            
            // ✅ structuredData에서 실제 이력서 이름 추출
            String realName = extractName(resume.getStructuredData());
            
            // ✅ 이력서에 이름이 없으면 User 테이블의 이름 사용
            if (realName == null || realName.isEmpty() || realName.equals("익명")) {
                User user = userRepository.findById(resume.getUserId()).orElse(null);
                if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                    realName = user.getName();
                    log.info("이력서에 이름 없음, User 이름 사용: {}", realName);
                }
            }

            // 이름 마스킹 (예: 김철수 -> 김**)
            String maskedName = maskName(realName);
            log.info("원래 이름: {}, 마스킹 이름: {}", realName, maskedName);

            // 기술 스택 파싱
            List<String> skillsList = parseSkills(resume.getSkills());
            log.info("기술 스택: {}", skillsList);

            // ✅ structuredData에서 실제 데이터 추출
            String location = extractLocation(resume.getStructuredData());
            int experienceYears = calculateExperienceYears(resume.getStructuredData());
            String salaryRange = extractSalaryRange(resume.getStructuredData());
            
            log.info("추출된 데이터 - 지역: {}, 경력: {}년, 연봉: {}", location, experienceYears, salaryRange);

            // 매칭 점수 계산 (임시로 80-95 사이 랜덤)
            int matchScore = 80 + (int)(Math.random() * 16);

            // ✅ 연락 상태 확인
            String contactStatus = null;
            if (companyUserId != null) {
                List<TalentContact> contacts = talentContactRepository
                        .findByResumeIdOrderByCreatedAtDesc(resume.getResumeId());
                
                // 해당 기업이 보낸 연락 중 가장 최근 것의 상태
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
                    .location(location)
                    .experienceYears(experienceYears)
                    .salaryRange(salaryRange)
                    .matchScore(matchScore)
                    .isAvailable(true)
                    .viewCount(resume.getViewCount())
                    .contactStatus(contactStatus) // ✅ 연락 상태 추가
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
        
        // 한글 이름인 경우: 성만 표시 (예: "김철수" -> "김**")
        if (isKorean(name.charAt(0))) {
            return name.charAt(0) + "**";
        }
        
        // 영문 이름인 경우: 첫 글자만 표시 (예: "admin" -> "a**", "John" -> "J**")
        return name.charAt(0) + "**";
    }
    
    private boolean isKorean(char c) {
        return (c >= '\uac00' && c <= '\ud7a3');
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

    /**
     * structuredData에서 지역 추출
     */
    private String extractLocation(String structuredData) {
        if (structuredData == null || structuredData.isEmpty()) {
            return "미지정";
        }

        try {
            JsonNode root = objectMapper.readTree(structuredData);
            JsonNode personalInfo = root.get("personalInfo");
            
            if (personalInfo != null && personalInfo.has("address")) {
                String address = personalInfo.get("address").asText();
                // 주소에서 시/도 추출 (예: "서울특별시 강남구" -> "서울")
                if (address.contains("서울")) return "서울";
                if (address.contains("경기")) return "경기";
                if (address.contains("부산")) return "부산";
                if (address.contains("대구")) return "대구";
                if (address.contains("인천")) return "인천";
                if (address.contains("광주")) return "광주";
                if (address.contains("대전")) return "대전";
                if (address.contains("울산")) return "울산";
                if (address.contains("세종")) return "세종";
                return address.split(" ")[0]; // 처음 단어 반환
            }
        } catch (Exception e) {
            log.warn("지역 추출 실패: {}", e.getMessage());
        }

        return "미지정";
    }

    /**
     * structuredData에서 경력 년수 계산
     */
    private int calculateExperienceYears(String structuredData) {
        if (structuredData == null || structuredData.isEmpty()) {
            return 0;
        }

        try {
            JsonNode root = objectMapper.readTree(structuredData);
            JsonNode careers = root.get("careers");
            
            if (careers != null && careers.isArray() && careers.size() > 0) {
                int totalMonths = 0;
                
                for (JsonNode career : careers) {
                    if (career.has("period")) {
                        String period = career.get("period").asText();
                        // period 형식: "2019.2 ~ 2023.5" 또는 "2019. 2 ~ 2023.5"
                        totalMonths += parsePeriodToMonths(period);
                    }
                }
                
                return totalMonths / 12; // 개월을 년으로 변환
            }
        } catch (Exception e) {
            log.warn("경력 계산 실패: {}", e.getMessage());
        }

        return 0; // 경력 없음 = 신입
    }

    /**
     * 기간 문자열을 개월수로 변환
     */
    private int parsePeriodToMonths(String period) {
        try {
            // "2019.2 ~ 2023.5" 형식 파싱
            String[] parts = period.split("~");
            if (parts.length != 2) return 0;
            
            String start = parts[0].trim().replace(" ", "");
            String end = parts[1].trim().replace(" ", "");
            
            // "2019.2" -> [2019, 2]
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
     * structuredData에서 이름 추출
     */
    private String extractName(String structuredData) {
        if (structuredData == null || structuredData.isEmpty()) {
            log.warn("structuredData가 비어있음");
            return "익명";
        }

        try {
            log.info("이름 추출 시도 - structuredData: {}", structuredData.substring(0, Math.min(200, structuredData.length())));
            JsonNode root = objectMapper.readTree(structuredData);
            JsonNode personalInfo = root.get("personalInfo");
            
            if (personalInfo != null) {
                log.info("personalInfo 존재: {}", personalInfo.toString());
                if (personalInfo.has("name")) {
                    String name = personalInfo.get("name").asText();
                    log.info("추출된 이름: '{}'", name);
                    if (name != null && !name.isEmpty() && !name.equals("null")) {
                        return name;
                    }
                } else {
                    log.warn("personalInfo에 name 필드가 없음");
                }
            } else {
                log.warn("structuredData에 personalInfo가 없음");
            }
        } catch (Exception e) {
            log.error("이름 추출 실패: {}", e.getMessage(), e);
        }

        return "익명";
    }

    /**
     * structuredData에서 희망연봉 추출
     */
    private String extractSalaryRange(String structuredData) {
        if (structuredData == null || structuredData.isEmpty()) {
            return "협의";
        }

        try {
            JsonNode root = objectMapper.readTree(structuredData);
            
            // structuredData에 salaryRange 필드가 있다면 사용
            if (root.has("salaryRange")) {
                return root.get("salaryRange").asText();
            }
            
            // 없으면 경력에 따라 기본값 추정
            int experienceYears = calculateExperienceYears(structuredData);
            if (experienceYears == 0) return "신입";
            if (experienceYears <= 2) return "3,000~4,000만원";
            if (experienceYears <= 4) return "4,000~5,500만원";
            if (experienceYears <= 7) return "5,000~7,000만원";
            return "7,000만원 이상";
        } catch (Exception e) {
            log.warn("희망연봉 추출 실패: {}", e.getMessage());
        }

        return "협의";
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

    /**
     * structuredData에 포트폴리오 및 자기소개서 ID 업데이트
     */
    @Transactional
    public void updateStructuredDataWithIds(Long resumeId, Long userId) {
        log.info("=== structuredData ID 업데이트 시작 - resumeId: {}, userId: {} ===", resumeId, userId);

        Resume resume = resumeRepository.findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (resume.getStructuredData() == null || resume.getStructuredData().isEmpty()) {
            log.warn("structuredData가 비어있음 - resumeId: {}", resumeId);
            return;
        }

        try {
            log.info("기존 structuredData: {}", resume.getStructuredData());

            JsonNode root = objectMapper.readTree(resume.getStructuredData());
            ObjectNode rootNode = (ObjectNode) root;
            boolean updated = false;

            // 포트폴리오 ID 추가
            List<Portfolio> portfolios = portfolioRepository.findByResumeIdOrderByDisplayOrder(resumeId);
            log.info("조회된 포트폴리오 개수: {}", portfolios != null ? portfolios.size() : 0);

            if (portfolios != null && !portfolios.isEmpty()) {
                ArrayNode portfoliosArray = objectMapper.createArrayNode();

                for (Portfolio portfolio : portfolios) {
                    log.info("포트폴리오 추가 - ID: {}, 파일명: {}", portfolio.getPortfolioId(), portfolio.getFileName());

                    ObjectNode portfolioNode = objectMapper.createObjectNode();
                    portfolioNode.put("portfolioId", portfolio.getPortfolioId());
                    portfolioNode.put("filename", portfolio.getFileName());
                    portfolioNode.put("fileType", portfolio.getFileType());
                    if (portfolio.getDescription() != null) {
                        portfolioNode.put("description", portfolio.getDescription());
                    }
                    portfoliosArray.add(portfolioNode);
                }

                rootNode.set("portfolios", portfoliosArray);
                updated = true;
                log.info("포트폴리오 배열 업데이트 완료");
            }

            // 자기소개서 ID 추가
            List<CoverLetter> coverLetters = coverLetterRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.info("조회된 자기소개서 개수: {}", coverLetters != null ? coverLetters.size() : 0);

            if (coverLetters != null && !coverLetters.isEmpty()) {
                ObjectNode coverLetterNode;

                if (rootNode.has("coverLetter")) {
                    coverLetterNode = (ObjectNode) rootNode.get("coverLetter");
                    log.info("기존 coverLetter 노드 사용");
                } else {
                    coverLetterNode = objectMapper.createObjectNode();
                    log.info("새 coverLetter 노드 생성");
                }

                ArrayNode filesArray = objectMapper.createArrayNode();
                for (CoverLetter coverLetter : coverLetters) {
                    if (coverLetter.getFilePath() != null) {
                        log.info("자기소개서 파일 추가 - ID: {}, 제목: {}", coverLetter.getCoverLetterId(), coverLetter.getTitle());

                        ObjectNode fileNode = objectMapper.createObjectNode();
                        fileNode.put("coverLetterId", coverLetter.getCoverLetterId());
                        String filename = coverLetter.getTitle() +
                                (coverLetter.getFileType() != null ? "." + coverLetter.getFileType() : "");
                        fileNode.put("filename", filename);
                        filesArray.add(fileNode);
                    }
                }

                if (filesArray.size() > 0) {
                    coverLetterNode.set("files", filesArray);
                    rootNode.set("coverLetter", coverLetterNode);
                    updated = true;
                    log.info("자기소개서 파일 배열 업데이트 완료");
                }
            }

            if (updated) {
                // 업데이트된 structuredData 저장
                String updatedData = objectMapper.writeValueAsString(rootNode);
                log.info("업데이트된 structuredData: {}", updatedData);

                resume.setStructuredData(updatedData);
                resumeRepository.save(resume);

                log.info("✅ structuredData 업데이트 완료 - resumeId: {}", resumeId);
            } else {
                log.info("업데이트할 포트폴리오/자기소개서 없음");
            }

        } catch (Exception e) {
            log.error("❌ structuredData 업데이트 실패", e);
            throw new RuntimeException("structuredData 업데이트에 실패했습니다", e);
        }
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
        Resume.Visibility visibility = Resume.Visibility.PUBLIC;
        if (request.getVisibility() != null) {
            try {
                visibility = Resume.Visibility.valueOf(request.getVisibility().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 visibility 값: {}", request.getVisibility());
            }
        }

        Resume resume = Resume.builder()
                .userId(userId)
                .title(request.getTitle())
                .jobCategory(request.getJobCategory())
                .structuredData(request.getSections())
                .skills(request.getSkills())
                .visibility(visibility)
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .build();

        resume = resumeRepository.save(resume);
        log.info("이력서 생성 완료 - resumeId: {}", resume.getResumeId());

        // 2. 포트폴리오 파일 저장
        if (portfolioFiles != null && !portfolioFiles.isEmpty()) {
            int displayOrder = 0;
            for (MultipartFile file : portfolioFiles) {
                try {
                    // 파일 저장
                    String filename = fileStorageService.saveFile(file);
                    String filePath = fileStorageService.getFileUrl(filename);
                    String fileType = getFileExtension(file.getOriginalFilename());

                    // Portfolio 엔티티 생성
                    Portfolio portfolio = Portfolio.builder()
                            .resume(resume)
                            .fileName(file.getOriginalFilename())
                            .filePath(filePath)
                            .fileType(fileType)
                            .fileSize(file.getSize())
                            .displayOrder(displayOrder++)
                            .build();

                    portfolioRepository.save(portfolio);
                    log.info("포트폴리오 저장 완료 - portfolioId: {}, filename: {}",
                            portfolio.getPortfolioId(), portfolio.getFileName());

                } catch (Exception e) {
                    log.error("포트폴리오 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 3. 자기소개서 파일 저장
        if (coverLetterFiles != null && !coverLetterFiles.isEmpty()) {
            for (MultipartFile file : coverLetterFiles) {
                try {
                    // 파일 저장
                    String filename = fileStorageService.saveFile(file);
                    String filePath = fileStorageService.getFileUrl(filename);
                    String fileType = getFileExtension(file.getOriginalFilename());

                    // CoverLetter 엔티티 생성
                    CoverLetter coverLetter = CoverLetter.builder()
                            .userId(userId)
                            .title(file.getOriginalFilename())
                            .filePath(filePath)
                            .fileType(fileType)
                            .build();

                    coverLetterRepository.save(coverLetter);
                    log.info("자기소개서 저장 완료 - coverLetterId: {}, filename: {}",
                            coverLetter.getCoverLetterId(), coverLetter.getTitle());

                } catch (Exception e) {
                    log.error("자기소개서 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 4. structuredData에 ID 추가
        updateStructuredDataWithIds(resume.getResumeId(), userId);

        // 5. 업데이트된 이력서 다시 조회
        resume = resumeRepository.findById(resume.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        return convertToResponse(resume);
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

        Resume resume = resumeRepository.findByResumeIdAndUserIdAndDeletedAtIsNull(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없거나 접근 권한이 없습니다"));

        // 1. 기본 정보 업데이트
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
        if (request.getSkills() != null) {
            resume.setSkills(request.getSkills());
        }
        if (request.getVisibility() != null) {
            try {
                Resume.Visibility visibility = Resume.Visibility.valueOf(request.getVisibility().toUpperCase());
                resume.setVisibility(visibility);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 visibility 값: {}", request.getVisibility());
            }
        }

        resume = resumeRepository.save(resume);

        // 2. 새 포트폴리오 파일 추가 (기존 파일은 유지)
        if (portfolioFiles != null && !portfolioFiles.isEmpty()) {
            // 기존 포트폴리오 개수 확인
            List<Portfolio> existingPortfolios = portfolioRepository.findByResumeIdOrderByDisplayOrder(resumeId);
            int displayOrder = existingPortfolios.size();

            for (MultipartFile file : portfolioFiles) {
                try {
                    String filename = fileStorageService.saveFile(file);
                    String filePath = fileStorageService.getFileUrl(filename);
                    String fileType = getFileExtension(file.getOriginalFilename());

                    Portfolio portfolio = Portfolio.builder()
                            .resume(resume)
                            .fileName(file.getOriginalFilename())
                            .filePath(filePath)
                            .fileType(fileType)
                            .fileSize(file.getSize())
                            .displayOrder(displayOrder++)
                            .build();

                    portfolioRepository.save(portfolio);
                    log.info("포트폴리오 추가 완료 - portfolioId: {}", portfolio.getPortfolioId());

                } catch (Exception e) {
                    log.error("포트폴리오 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 3. 새 자기소개서 파일 추가 (기존 파일은 유지)
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
                    log.info("자기소개서 추가 완료 - coverLetterId: {}", coverLetter.getCoverLetterId());

                } catch (Exception e) {
                    log.error("자기소개서 파일 저장 실패: {}", file.getOriginalFilename(), e);
                }
            }
        }

        // 4. structuredData에 ID 추가
        updateStructuredDataWithIds(resumeId, userId);

        // 5. 업데이트된 이력서 다시 조회
        resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        return convertToResponse(resume);
    }
}
