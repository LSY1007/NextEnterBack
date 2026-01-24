package org.zerock.nextenter.apply.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.apply.dto.ApplyListResponse;
import org.zerock.nextenter.apply.dto.ApplyRequest;
import org.zerock.nextenter.apply.dto.ApplyResponse;
import org.zerock.nextenter.apply.dto.ApplyStatusUpdateRequest;
import org.zerock.nextenter.apply.entity.Apply;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.JobPostingRepository;
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
public class ApplyService {

    private final ApplyRepository applyRepository;
    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeRepository resumeRepository;
    private final org.zerock.nextenter.notification.NotificationService notificationService;

    /**
     * 지원하기 (개인회원용)
     */
    @Transactional
    public ApplyResponse createApply(Long userId, ApplyRequest request) {
        log.info("지원 등록 - userId: {}, jobId: {}, resumeId: {}",
                userId, request.getJobId(), request.getResumeId());

        // 중복 지원 확인
        boolean alreadyApplied = applyRepository.existsByUserIdAndJobId(userId, request.getJobId());
        if (alreadyApplied) {
            throw new IllegalStateException("이미 지원한 공고입니다");
        }

        // 공고 유효성 검증
        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

        if (job.getStatus() != JobPosting.Status.ACTIVE) {
            throw new IllegalStateException("마감된 공고입니다");
        }

        // 이력서 유효성 검증
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (!resume.getUserId().equals(userId)) {
            throw new IllegalArgumentException("자신의 이력서만 사용할 수 있습니다");
        }

        // 지원 생성
        Apply apply = Apply.builder()
                .userId(userId)
                .jobId(request.getJobId())
                .resumeId(request.getResumeId())
                .coverLetterId(request.getCoverLetterId())
                .status(Apply.Status.PENDING)
                .build();

        apply = applyRepository.save(apply);
        
        // 공고의 지원자 수 증가
        log.info("지원자 수 증가 전 - jobId: {}", request.getJobId());
        jobPostingRepository.incrementApplicantCount(request.getJobId());
        log.info("지원자 수 증가 후 - jobId: {}", request.getJobId());
        
        // 실제 지원자 수 확인
        Long actualCount = applyRepository.countByJobId(request.getJobId());
        log.info("실제 지원자 수 - jobId: {}, count: {}", request.getJobId(), actualCount);
        
        // 기업에 새로운 지원자 알림 전송
        try {
            log.info("알림 전송 시도 - companyId: {}, jobTitle: {}", 
                job.getCompanyId(), job.getTitle());
            
            notificationService.notifyNewApplication(
                job.getCompanyId(),
                job.getTitle(),
                apply.getApplyId()
            );
            
            log.info("새 지원자 알림 전송 성공!");
        } catch (Exception e) {
            log.error("새 지원자 알림 전송 실패", e);
        }
        
        log.info("지원 완료 - applyId: {}", apply.getApplyId());

        return convertToDetailResponse(apply);
    }

    /**
     * 내 지원 내역 조회 (개인회원용) - 단순 목록
     */
    public List<ApplyListResponse> getMyApplies(Long userId) {
        log.info("내 지원 내역 조회 - userId: {}", userId);

        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);
        
        return applies.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 내 지원 내역 조회 (개인회원용) - 페이징
     */
    public Page<ApplyListResponse> getMyApplications(Long userId, int page, int size) {
        log.info("내 지원 내역 조회 - userId: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);
        
        // List를 Page로 변환
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), applies.size());
        List<Apply> pageContent = applies.subList(start, end);
        
        Page<Apply> applyPage = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, applies.size()
        );

        return applyPage.map(this::convertToListResponse);
    }

    /**
     * 기업의 모든 지원자 조회 (페이징)
     */
    public Page<ApplyListResponse> getAppliesByCompany(
            Long companyId, Long jobId, int page, int size) {

        log.info("기업 지원자 조회 - companyId: {}, jobId: {}", companyId, jobId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Apply> applies;

        if (jobId != null) {
            // 특정 공고의 지원자만 조회
            applies = applyRepository.findByJobIdPaged(jobId, pageable);
        } else {
            // 기업의 모든 공고에 대한 지원자 조회
            applies = applyRepository.findByCompanyId(companyId, pageable);
        }

        return applies.map(this::convertToListResponse);
    }

    /**
     * 지원자 상세 조회
     */
    public ApplyResponse getApplyDetail(Long applyId, Long companyId) {
        log.info("지원자 상세 조회 - applyId: {}, companyId: {}", applyId, companyId);

        Apply apply = applyRepository.findByIdAndCompanyId(applyId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원 내역을 찾을 수 없거나 접근 권한이 없습니다"));

        return convertToDetailResponse(apply);
    }

    /**
     * 지원 상태 변경 (합격/불합격 등)
     */
    @Transactional
    public ApplyResponse updateApplyStatus(
            Long applyId, Long companyId, ApplyStatusUpdateRequest request) {

        log.info("지원 상태 변경 - applyId: {}, status: {}", applyId, request.getStatus());

        Apply apply = applyRepository.findByIdAndCompanyId(applyId, companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원 내역을 찾을 수 없거나 접근 권한이 없습니다"));

        // 상태 변경
        Apply.Status newStatus = Apply.Status.valueOf(request.getStatus());
        apply.setStatus(newStatus);
        apply.setNotes(request.getNotes());
        apply.setReviewedAt(LocalDateTime.now());

        applyRepository.save(apply);
        
        // 지원자에게 상태 변경 알림 전송
        try {
            JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
            if (job != null) {
                // 회사명 가져오기
                User companyUser = userRepository.findById(job.getCompanyId()).orElse(null);
                String companyName = companyUser != null && companyUser.getName() != null
                    ? companyUser.getName() : job.getTitle();
                
                String statusText = getStatusText(newStatus);
                
                log.info("알림 전송 시도 - userId: {}, companyName: {}, status: {}",
                    apply.getUserId(), companyName, statusText);
                
                notificationService.notifyApplicationStatus(
                    apply.getUserId(),
                    companyName,
                    statusText,
                    apply.getApplyId()
                );
                
                log.info("지원 상태 변경 알림 전송 성공!");
            }
        } catch (Exception e) {
            log.error("지원 상태 변경 알림 전송 실패", e);
        }

        return convertToDetailResponse(apply);
    }

    // Private helper methods
    
    private String getStatusText(Apply.Status status) {
        switch (status) {
            case PENDING: return "검토 중";
            case REVIEWING: return "서류 검토중";
            case ACCEPTED: return "합격";
            case REJECTED: return "불합격";
            case CANCELED: return "지원 취소";
            default: return status.name();
        }
    }

    private ApplyListResponse convertToListResponse(Apply apply) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);
        JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
        Resume resume = apply.getResumeId() != null ?
                resumeRepository.findById(apply.getResumeId()).orElse(null) : null;

        // 기술 스택 파싱 (JSON 배열 또는 쉼표 구분 문자열)
        List<String> skills = List.of();
        if (resume != null && resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            try {
                // JSON 배열로 파싱 시도
                if (resume.getSkills().trim().startsWith("[")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    skills = mapper.readValue(resume.getSkills(), 
                            mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                } else {
                    // 쉼표 구분 문자열로 파싱
                    skills = Arrays.stream(resume.getSkills().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("skills 파싱 실패, 쉼표 구분으로 재시도: {}", e.getMessage());
                skills = Arrays.stream(resume.getSkills().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        }

        // 경력 계산 (임시 - 추후 Resume에서 파싱)
        String experience = "5년"; // TODO: structuredData에서 파싱

        return ApplyListResponse.builder()
                .applyId(apply.getApplyId())
                .userId(apply.getUserId())
                .jobId(apply.getJobId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .skills(skills)
                .experience(experience)
                .status(apply.getStatus().name())
                .aiScore(apply.getAiScore())
                .appliedAt(apply.getAppliedAt())
                .build();
    }

    private ApplyResponse convertToDetailResponse(Apply apply) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);
        JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
        
        // 이력서 정보 가져오기
        Resume resume = apply.getResumeId() != null ?
                resumeRepository.findById(apply.getResumeId()).orElse(null) : null;
        
        // 기술 스택 파싱 (JSON 배열 또는 쉼표 구분 문자열)
        List<String> skills = List.of();
        if (resume != null && resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            try {
                // JSON 배열로 파싱 시도
                if (resume.getSkills().trim().startsWith("[")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    skills = mapper.readValue(resume.getSkills(), 
                            mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                } else {
                    // 쉼표 구분 문자열로 파싱
                    skills = Arrays.stream(resume.getSkills().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("skills 파싱 실패, 쉼표 구분으로 재시도: {}", e.getMessage());
                skills = Arrays.stream(resume.getSkills().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        }

        // structuredData에서 상세 정보 추출
        String education = null;
        String certifications = null;
        String coverLetterContent = null;
        String experience = "5년"; // 기본값
        
        if (resume != null && resume.getStructuredData() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(resume.getStructuredData());
                
                // 학력 정보 추출
                if (root.has("educations")) {
                    com.fasterxml.jackson.databind.JsonNode educations = root.get("educations");
                    StringBuilder eduBuilder = new StringBuilder();
                    for (com.fasterxml.jackson.databind.JsonNode edu : educations) {
                        if (edu.has("school") && edu.has("period")) {
                            eduBuilder.append(edu.get("school").asText())
                                    .append(" | ")
                                    .append(edu.get("period").asText())
                                    .append("\n");
                        }
                    }
                    if (eduBuilder.length() > 0) {
                        education = eduBuilder.toString().trim();
                    }
                }
                
                // 자격증 정보 추출
                if (root.has("certificates")) {
                    com.fasterxml.jackson.databind.JsonNode certificates = root.get("certificates");
                    StringBuilder certBuilder = new StringBuilder();
                    for (com.fasterxml.jackson.databind.JsonNode cert : certificates) {
                        if (cert.has("title") && cert.has("date")) {
                            certBuilder.append(cert.get("title").asText())
                                    .append(" | ")
                                    .append(cert.get("date").asText())
                                    .append("\n");
                        }
                    }
                    if (certBuilder.length() > 0) {
                        certifications = certBuilder.toString().trim();
                    }
                }
                
                // 경력 정보 추출
                if (root.has("careers")) {
                    com.fasterxml.jackson.databind.JsonNode careers = root.get("careers");
                    if (careers.isArray() && careers.size() > 0) {
                        int totalMonths = 0;
                        for (com.fasterxml.jackson.databind.JsonNode career : careers) {
                            if (career.has("period")) {
                                String period = career.get("period").asText();
                                totalMonths += parsePeriodToMonths(period);
                            }
                        }
                        int years = totalMonths / 12;
                        experience = years > 0 ? years + "년" : "신입";
                    }
                } else {
                    experience = "신입"; // 경력 없으면 신입
                }
                
                // 자기소개서 정보 추출
                if (root.has("coverLetter")) {
                    com.fasterxml.jackson.databind.JsonNode coverLetter = root.get("coverLetter");
                    if (coverLetter.has("content")) {
                        coverLetterContent = coverLetter.get("content").asText();
                    }
                }
            } catch (Exception e) {
                log.warn("이력서 structuredData 파싱 실패: {}", e.getMessage());
            }
        }

        return ApplyResponse.builder()
                .applyId(apply.getApplyId())
                .userId(apply.getUserId())
                .jobId(apply.getJobId())
                .resumeId(apply.getResumeId())
                .coverLetterId(apply.getCoverLetterId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userPhone(user != null ? user.getPhone() : null)
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                // 이력서 정보
                .resumeTitle(resume != null ? resume.getTitle() : null)
                .skills(skills)
                .experience(experience)
                .education(education)
                .certifications(certifications)
                // 자기소개서 정보
                .coverLetterContent(coverLetterContent)
                .status(apply.getStatus().name())
                .aiScore(apply.getAiScore())
                .notes(apply.getNotes())
                .appliedAt(apply.getAppliedAt())
                .reviewedAt(apply.getReviewedAt())
                .updatedAt(apply.getUpdatedAt())
                .build();
    }
    
    // 기간 문자열을 개월수로 변환
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
}
