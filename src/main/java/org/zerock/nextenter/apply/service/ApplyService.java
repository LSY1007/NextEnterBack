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
    private final org.zerock.nextenter.coverletter.repository.CoverLetterRepository coverLetterRepository;

    @Deprecated
    @Transactional
    public ApplyResponse createInterviewRequest(Long companyId, Long userId, Long jobId) {
        throw new UnsupportedOperationException(
                "이 메서드는 더 이상 사용되지 않습니다. InterviewOfferService.createOffer()를 사용하세요.");
    }

    /**
     * 지원하기 (개인회원용)
     */
    @Transactional
    public ApplyResponse createApply(Long userId, ApplyRequest request) {
        log.info("▶ [ApplyService] 지원 등록 시작 - userId: {}, jobId: {}", userId, request.getJobId());

        boolean alreadyApplied = applyRepository.existsByUserIdAndJobId(userId, request.getJobId());
        if (alreadyApplied) {
            log.error("❌ 이미 지원한 공고입니다 - userId: {}, jobId: {}", userId, request.getJobId());
            throw new IllegalStateException("이미 지원한 공고입니다");
        }

        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

        if (job.getStatus() != JobPosting.Status.ACTIVE) {
            throw new IllegalStateException("마감된 공고입니다");
        }

        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (!resume.getUserId().equals(userId)) {
            throw new IllegalArgumentException("자신의 이력서만 사용할 수 있습니다");
        }

        // ✅ [수정] DocumentStatus 사용 (Status 필드 없음)
        Apply apply = Apply.builder()
                .userId(userId)
                .jobId(request.getJobId())
                .resumeId(request.getResumeId())
                .coverLetterId(request.getCoverLetterId())
                .documentStatus(Apply.DocumentStatus.PENDING)
                .finalStatus(null)
                .build();

        apply = applyRepository.save(apply);
        log.info("✅ 지원 정보 저장 완료 - applyId: {}", apply.getApplyId());

        jobPostingRepository.incrementApplicantCount(request.getJobId());

        try {
            notificationService.notifyNewApplication(
                    job.getCompanyId(), job.getTitle(), apply.getApplyId()
            );
        } catch (Exception e) {
            log.error("⚠️ 알림 전송 실패 (지원은 정상 처리됨)", e);
        }

        return convertToDetailResponse(apply);
    }

    public List<ApplyListResponse> getMyApplies(Long userId) {
        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);
        return applies.stream().map(this::convertToListResponse).collect(Collectors.toList());
    }

    public Page<ApplyListResponse> getMyApplications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Apply> applies = applyRepository.findByUserIdOrderByAppliedAtDesc(userId);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), applies.size());
        List<Apply> pageContent = applies.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, applies.size()
        ).map(this::convertToListResponse);
    }

    public Page<ApplyListResponse> getAppliesByCompany(Long companyId, Long jobId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Apply> applies;
        if (jobId != null) {
            applies = applyRepository.findByJobIdPaged(jobId, pageable);
        } else {
            applies = applyRepository.findByCompanyId(companyId, pageable);
        }
        return applies.map(this::convertToListResponse);
    }

    public ApplyResponse getApplyDetail(Long applyId, Long companyId) {
        Apply apply = applyRepository.findByIdAndCompanyId(applyId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다"));
        return convertToDetailResponse(apply);
    }

    @Transactional
    public ApplyResponse updateApplyStatus(Long applyId, Long companyId, ApplyStatusUpdateRequest request) {
        log.info("▶ [ApplyService] 지원 상태 변경 요청 - applyId: {}, status: {}", applyId, request.getStatus());

        Apply apply = applyRepository.findByIdAndCompanyId(applyId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다"));

        // ✅ [수정] 입력값에 따라 DocumentStatus 또는 FinalStatus 업데이트
        String statusValue = request.getStatus();

        try {
            if (statusValue.equals("ACCEPTED")) {
                apply.setFinalStatus(Apply.FinalStatus.PASSED);
            } else if (statusValue.equals("REJECTED") || statusValue.equals("CANCELED")) {
                apply.setFinalStatus(Apply.FinalStatus.valueOf(statusValue));
            } else {
                apply.setDocumentStatus(Apply.DocumentStatus.valueOf(statusValue));
            }
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ 알 수 없는 상태값: {}. 기본값(REVIEWING)으로 설정합니다.", statusValue);
            apply.setDocumentStatus(Apply.DocumentStatus.REVIEWING);
        }

        apply.setNotes(request.getNotes());
        apply.setReviewedAt(LocalDateTime.now());
        applyRepository.save(apply);
        log.info("✅ 상태 변경 완료 - DocumentStatus: {}, FinalStatus: {}", apply.getDocumentStatus(), apply.getFinalStatus());

        try {
            JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
            if (job != null) {
                User companyUser = userRepository.findById(job.getCompanyId()).orElse(null);
                String companyName = companyUser != null ? companyUser.getName() : job.getTitle();

                String statusText = convertToLegacyStatus(apply);

                notificationService.notifyApplicationStatus(
                        apply.getUserId(), companyName, statusText, apply.getApplyId()
                );
            }
        } catch (Exception e) {
            log.error("알림 전송 실패", e);
        }

        return convertToDetailResponse(apply);
    }

    @Deprecated
    @Transactional
    public ApplyResponse updateInterviewStatus(Long applyId, Long companyId, String interviewStatus) {
        throw new UnsupportedOperationException("InterviewOfferService를 사용하세요.");
    }

    // ✅ [지원 취소 기능] 로그 포함 + DocumentStatus/FinalStatus 모두 취소 처리
    @Transactional
    public void cancelApply(Long userId, Long applyId) {
        log.info("========== [ApplyService] 지원 취소 요청 시작 (User: {}, Apply: {}) ==========", userId, applyId);

        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> {
                    log.error("❌ [오류] 지원 내역 없음 - applyId: {}", applyId);
                    return new IllegalArgumentException("지원 내역을 찾을 수 없습니다.");
                });

        if (!apply.getUserId().equals(userId)) {
            log.error("❌ [오류] 권한 없음 - 내 지원 내역 아님 (RequestUser: {}, Owner: {})", userId, apply.getUserId());
            throw new IllegalArgumentException("본인의 지원 내역만 취소할 수 있습니다.");
        }

        log.info("🔍 변경 전 상태 - Document: {}, Final: {}", apply.getDocumentStatus(), apply.getFinalStatus());

        // ✅ Apply.java에 있는 Enum 값 사용 (Status 아님!)
        apply.setFinalStatus(Apply.FinalStatus.CANCELED);
        apply.setDocumentStatus(Apply.DocumentStatus.CANCELED);

        // 명시적 저장 (로그 확인용)
        Apply savedApply = applyRepository.save(apply);

        log.info("✅ 변경 후 상태 - Document: {}, Final: {}", savedApply.getDocumentStatus(), savedApply.getFinalStatus());
        log.info("========== [ApplyService] 지원 취소 완료 ==========");
    }

    // Private helper methods

    // ✅ [필수] 프론트엔드 호환용 상태 변환 (이게 없으면 컴파일 에러남)
    private String convertToLegacyStatus(Apply apply) {
        if (apply.getFinalStatus() != null) {
            if (apply.getFinalStatus() == Apply.FinalStatus.PASSED) return "ACCEPTED";
            return apply.getFinalStatus().name(); // CANCELED, REJECTED 등 반환
        }
        if (apply.getDocumentStatus() != null) {
            if (apply.getDocumentStatus() == Apply.DocumentStatus.PASSED) return "ACCEPTED";
            return apply.getDocumentStatus().name();
        }
        return "PENDING";
    }

    private ApplyListResponse convertToListResponse(Apply apply) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);
        JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
        Resume resume = apply.getResumeId() != null ?
                resumeRepository.findById(apply.getResumeId()).orElse(null) : null;

        List<String> skills = parseSkills(resume);
        String companyName = (job != null) ?
                (userRepository.findById(job.getCompanyId()).map(User::getName).orElse("알 수 없음")) : "알 수 없음";

        return ApplyListResponse.builder()
                .applyId(apply.getApplyId())
                .userId(apply.getUserId())
                .jobId(apply.getJobId())
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .companyName(companyName)
                .location(job != null ? job.getLocation() : "")
                .deadline(job != null && job.getDeadline() != null ? job.getDeadline().toString() : "")
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .skills(skills)
                .experience("신입")
                // ✅ getStatus() 대신 변환 메서드 사용 (에러 방지)
                .status(convertToLegacyStatus(apply))
                .aiScore(apply.getAiScore())
                .appliedAt(apply.getAppliedAt())
                .interviewStatus(null)
                .build();
    }

    private ApplyResponse convertToDetailResponse(Apply apply) {
        User user = userRepository.findById(apply.getUserId()).orElse(null);
        JobPosting job = jobPostingRepository.findById(apply.getJobId()).orElse(null);
        Resume resume = apply.getResumeId() != null ?
                resumeRepository.findById(apply.getResumeId()).orElse(null) : null;

        List<String> skills = parseSkills(resume);

        // ✅ Resume 테이블의 개인정보 컴럼에서 직접 가져오기
        String resumeName = resume != null ? resume.getResumeName() : null;
        String gender = resume != null ? resume.getResumeGender() : null;
        String birthDate = resume != null ? resume.getResumeBirthDate() : null;
        String email = resume != null ? resume.getResumeEmail() : null;
        String phone = resume != null ? resume.getResumePhone() : null;
        String address = resume != null ? resume.getResumeAddress() : null;
        String detailAddress = resume != null ? resume.getResumeDetailAddress() : null;
        String profileImage = resume != null ? resume.getProfileImage() : null;

        // 주소 합치기 (기본주소 + 상세주소)
        String fullAddress = null;
        if (address != null) {
            fullAddress = address;
            if (detailAddress != null && !detailAddress.isEmpty()) {
                fullAddress += " " + detailAddress;
            }
        }

        // ✅ 이력서 상세 정보 파싱 (experiences, certificates, educations, careers)
        List<ApplyResponse.ExperienceItem> experiences = parseExperiences(resume);
        List<ApplyResponse.CertificateItem> certificates = parseCertificates(resume);
        List<ApplyResponse.EducationItem> educations = parseEducations(resume);
        List<ApplyResponse.CareerItem> careers = parseCareers(resume);

        // ✅ 자기소개서 정보 가져오기
        String coverLetterTitle = null;
        String coverLetterContent = null;
        if (apply.getCoverLetterId() != null) {
            try {
                org.zerock.nextenter.coverletter.entity.CoverLetter coverLetter = 
                    coverLetterRepository.findById(apply.getCoverLetterId()).orElse(null);
                if (coverLetter != null) {
                    coverLetterTitle = coverLetter.getTitle();
                    coverLetterContent = coverLetter.getContent();
                }
            } catch (Exception e) {
                log.warn("⚠️ 자기소개서 조회 실패: {}", e.getMessage());
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
                .userEmail(email)
                .userPhone(phone)
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .resumeTitle(resume != null ? resume.getTitle() : null)
                .gender(gender)
                .birthDate(birthDate)
                .address(fullAddress)
                .profileImage(profileImage)
                .skills(skills)
                .experience("신입")
                // ✅ 이력서 상세 정보 추가
                .experiences(experiences)
                .certificates(certificates)
                .educations(educations)
                .careers(careers)
                // ✅ 자기소개서 정보 추가
                .coverLetterTitle(coverLetterTitle)
                .coverLetterContent(coverLetterContent)
                .status(convertToLegacyStatus(apply))
                .aiScore(apply.getAiScore())
                .notes(apply.getNotes())
                .appliedAt(apply.getAppliedAt())
                .reviewedAt(apply.getReviewedAt())
                .updatedAt(apply.getUpdatedAt())
                .build();
    }

    private List<String> parseSkills(Resume resume) {
        if (resume == null || resume.getSkills() == null || resume.getSkills().isEmpty()) return List.of();
        try {
            if (resume.getSkills().trim().startsWith("[")) {
                return new com.fasterxml.jackson.databind.ObjectMapper().readValue(resume.getSkills(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>(){});
            } else {
                return Arrays.stream(resume.getSkills().split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            }
        } catch (Exception e) { return List.of(); }
    }

    // ✅ 경험/활동/교육 파싱
    private List<ApplyResponse.ExperienceItem> parseExperiences(Resume resume) {
        if (resume == null || resume.getExperiences() == null || resume.getExperiences().isEmpty()) {
            return List.of();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode array = mapper.readTree(resume.getExperiences());
            List<ApplyResponse.ExperienceItem> result = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : array) {
                String title = node.has("title") ? node.get("title").asText() : "";
                String period = node.has("period") ? node.get("period").asText() : "";
                result.add(ApplyResponse.ExperienceItem.builder()
                        .title(title)
                        .period(period)
                        .build());
            }
            return result;
        } catch (Exception e) {
            log.warn("경험/활동/교육 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    // ✅ 자격증/어학/수상 파싱
    private List<ApplyResponse.CertificateItem> parseCertificates(Resume resume) {
        if (resume == null || resume.getCertificates() == null || resume.getCertificates().isEmpty()) {
            return List.of();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode array = mapper.readTree(resume.getCertificates());
            List<ApplyResponse.CertificateItem> result = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : array) {
                String title = node.has("title") ? node.get("title").asText() : "";
                String date = node.has("date") ? node.get("date").asText() : "";
                result.add(ApplyResponse.CertificateItem.builder()
                        .title(title)
                        .date(date)
                        .build());
            }
            return result;
        } catch (Exception e) {
            log.warn("자격증/어학/수상 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    // ✅ 학력 파싱
    private List<ApplyResponse.EducationItem> parseEducations(Resume resume) {
        if (resume == null || resume.getEducations() == null || resume.getEducations().isEmpty()) {
            return List.of();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode array = mapper.readTree(resume.getEducations());
            List<ApplyResponse.EducationItem> result = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : array) {
                String school = node.has("school") ? node.get("school").asText() : "";
                String period = node.has("period") ? node.get("period").asText() : "";
                result.add(ApplyResponse.EducationItem.builder()
                        .school(school)
                        .period(period)
                        .build());
            }
            return result;
        } catch (Exception e) {
            log.warn("학력 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    // ✅ 경력 파싱
    private List<ApplyResponse.CareerItem> parseCareers(Resume resume) {
        if (resume == null || resume.getCareers() == null || resume.getCareers().isEmpty()) {
            return List.of();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode array = mapper.readTree(resume.getCareers());
            List<ApplyResponse.CareerItem> result = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : array) {
                String company = node.has("company") ? node.get("company").asText() : "";
                String period = node.has("period") ? node.get("period").asText() : "";
                result.add(ApplyResponse.CareerItem.builder()
                        .company(company)
                        .period(period)
                        .build());
            }
            return result;
        } catch (Exception e) {
            log.warn("경력 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }
}