package org.zerock.nextenter.ai.resume.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.ai.resume.ResumeAiService;
import org.zerock.nextenter.ai.resume.dto.AiRecommendRequest;
import org.zerock.nextenter.ai.resume.dto.AiRecommendResponse;
import org.zerock.nextenter.ai.resume.entity.ResumeAiRecommend;
import org.zerock.nextenter.ai.resume.repository.ResumeAiRecommendRepository;
import org.zerock.nextenter.company.entity.Company;
import org.zerock.nextenter.company.repository.CompanyRepository;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.JobPostingRepository;
import org.zerock.nextenter.matching.entity.ResumeMatching;
import org.zerock.nextenter.matching.service.MatchingService;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.repository.ResumeRepository;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAiRecommendService {

    private final ResumeAiService resumeAiService;
    private final ResumeAiRecommendRepository recommendRepository;
    private final MatchingService matchingService;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AiRecommendResponse recommendAndSave(AiRecommendRequest request) {
        log.info("🚀 AI 추천 및 저장 프로세스 시작 (userId: {})", request.getUserId());

        // 0. resumeText가 비어있으면 DB에서 이력서 조회 후 request 보강 (웹 AI 버튼 500 해결)
        enrichRequestFromResume(request);

        // 1. 비서에게 시켜서 파이썬 서버 데이터 가져오기 (422 해결된 메서드 호출)
        AiRecommendResponse responseDto = resumeAiService.fetchRecommendation(request);

        // 2. 응답에 유저 정보 보강
        responseDto.setUserId(request.getUserId());

        // 2-1. 경력 기반 주니어/시니어 판별 후 응답에 설정
        ResumeMatching.ExperienceLevel expLevel = calculateExperienceLevel(request.getResumeId());
        responseDto.setExperienceLevel(expLevel.name());

        // 2-2. 추천 기업별 job_id + job_status 매칭 (회사명 → company → 경력레벨에 맞는 공고)
        if (responseDto.getCompanies() != null) {
            for (AiRecommendResponse.CompanyRecommend company : responseDto.getCompanies()) {
                matchJobForCompany(company, expLevel);
            }
        }

        // 3. resume_ai_recommend 테이블 저장
        try {
            saveToDatabase(request, responseDto);
        } catch (Exception e) {
            log.error("⚠️ [DB Error] AI 추천 저장 실패: {}", e.getMessage());
        }

        // 4. resume_matching 테이블 저장 (추천 기업별 매칭 기록)
        try {
            saveToMatchingTable(request, responseDto);
        } catch (Exception e) {
            log.error("⚠️ [DB Error] 매칭 테이블 저장 실패: {}", e.getMessage());
        }

        return responseDto;
    }

    /**
     * resumeId로 DB에서 이력서 조회 후 request를 보강한다.
     * 프론트엔드는 resumeId, userId만 전송하므로 resumeText 등이 비어있을 수 있다.
     */
    private void enrichRequestFromResume(AiRecommendRequest request) {
        if (request.getResumeText() != null && !request.getResumeText().toString().trim().isEmpty()) {
            log.debug("resumeText가 이미 존재하여 DB 조회 생략");
            return;
        }
        if (request.getResumeId() == null || request.getUserId() == null) {
            log.warn("resumeId 또는 userId가 없어 DB 보강 불가");
            return;
        }

        Resume resume = resumeRepository
                .findByResumeIdAndUserIdAndDeletedAtIsNull(request.getResumeId(), request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "이력서를 찾을 수 없거나 접근 권한이 없습니다. (resumeId=" + request.getResumeId() + ", userId="
                                + request.getUserId() + ")"));

        // 1. resumeText 구성: extractedText 우선, 없으면 구조화 필드로부터 생성
        String resumeText = buildResumeText(resume);
        if (resumeText != null && !resumeText.trim().isEmpty()) {
            request.setResumeText(resumeText);
        }

        // 2. jobCategory
        if (request.getJobCategory() == null && resume.getJobCategory() != null) {
            request.setJobCategory(resume.getJobCategory());
        }

        // 3. skills
        if (request.getSkills() == null
                || (request.getSkills() instanceof List && ((List<?>) request.getSkills()).isEmpty())) {
            request.setSkills(parseJsonToObject(resume.getSkills()));
        }

        // 4. educations
        if (request.getEducations() == null
                || (request.getEducations() instanceof List && ((List<?>) request.getEducations()).isEmpty())) {
            request.setEducations(parseJsonToObject(resume.getEducations()));
        }

        // 5. careers
        if (request.getCareers() == null
                || (request.getCareers() instanceof List && ((List<?>) request.getCareers()).isEmpty())) {
            request.setCareers(parseJsonToObject(resume.getCareers()));
        }

        // 6. projects (experiences)
        if (request.getProjects() == null
                || (request.getProjects() instanceof List && ((List<?>) request.getProjects()).isEmpty())) {
            request.setProjects(parseJsonToObject(resume.getExperiences()));
        }

        // 7. filePath (AI 서버가 파일 파싱 시 사용)
        if (request.getFilePath() == null && resume.getFilePath() != null && !resume.getFilePath().trim().isEmpty()) {
            request.setFilePath(resume.getFilePath());
        }

        // 8. resumeText가 여전히 비어있으면 사용자 안내 메시지로 예외
        if (request.getResumeText() == null || request.getResumeText().toString().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "이력서에 분석할 내용이 없습니다. 이력서를 작성하거나 파일을 업로드해주세요.");
        }

        // 9. 디버깅: 각 필드 상태 로그
        log.info("📊 [DB 이력서 필드 상태] resumeId={}", request.getResumeId());
        log.info("  - jobCategory: {}", resume.getJobCategory());
        log.info("  - extractedText: {}", resume.getExtractedText() != null ? resume.getExtractedText().length() + "글자" : "null");
        log.info("  - skills: {}", resume.getSkills() != null && !resume.getSkills().isEmpty() ? "있음" : "비어있음");
        log.info("  - educations: {}", resume.getEducations() != null && !resume.getEducations().isEmpty() ? "있음" : "비어있음");
        log.info("  - careers: {}", resume.getCareers() != null && !resume.getCareers().isEmpty() ? "있음" : "비어있음");
        log.info("  - experiences: {}", resume.getExperiences() != null && !resume.getExperiences().isEmpty() ? "있음" : "비어있음");
        log.info("  - filePath: {}", resume.getFilePath());
        log.info("  - resumeText (생성됨): {} 글자", request.getResumeText().toString().length());

        log.info("✅ 이력서 데이터 DB 보강 완료: resumeId={}", request.getResumeId());
    }

    /**
     * Resume 엔티티로부터 AI 분석용 텍스트를 생성합니다.
     * 1순위: extractedText (PDF/DOCX에서 추출한 원본)
     * 2순위: 구조화 필드(JSON)를 파싱하여 읽기 쉬운 텍스트로 변환
     */
    private String buildResumeText(Resume resume) {
        // 1순위: extractedText (원본 텍스트)
        if (resume.getExtractedText() != null && !resume.getExtractedText().trim().isEmpty()) {
            log.debug("✅ extractedText 사용 (길이: {} 글자)", resume.getExtractedText().length());
            return resume.getExtractedText();
        }

        // 2순위: 구조화 필드로부터 재구성
        log.warn("⚠️ extractedText가 없어 구조화 필드로부터 재구성 (resumeId: {})", resume.getResumeId());
        StringBuilder sb = new StringBuilder();

        // 기본 정보
        if (resume.getResumeName() != null) {
            sb.append("[이름]\n").append(resume.getResumeName()).append("\n\n");
        }
        if (resume.getJobCategory() != null) {
            sb.append("[희망 직무]\n").append(resume.getJobCategory()).append("\n\n");
        }

        // JSON 필드들을 파싱하여 텍스트로 변환
        appendJsonField(sb, "[보유 기술]", resume.getSkills());
        appendJsonField(sb, "[학력 사항]", resume.getEducations());
        appendJsonField(sb, "[경력 사항]", resume.getCareers());
        appendJsonField(sb, "[프로젝트 및 경험]", resume.getExperiences());
        appendJsonField(sb, "[자격증 및 어학]", resume.getCertificates());

        String result = sb.toString().trim();
        if (result.isEmpty()) {
            log.error("❌ 이력서에 분석 가능한 데이터가 전혀 없습니다 (resumeId: {})", resume.getResumeId());
            return null;
        }

        log.info("✅ 구조화 필드로부터 텍스트 재구성 완료 (길이: {} 글자)", result.length());
        return result;
    }

    /**
     * JSON 필드를 읽기 쉬운 텍스트로 변환하여 StringBuilder에 추가합니다.
     * [FIX] Map.toString() 대신 필드별로 읽기 좋은 형태로 변환
     */
    @SuppressWarnings("unchecked")
    private void appendJsonField(StringBuilder sb, String title, String jsonField) {
        if (jsonField == null || jsonField.trim().isEmpty() || jsonField.equals("[]")) {
            return;
        }

        try {
            Object parsed = objectMapper.readValue(jsonField, Object.class);
            if (parsed instanceof List) {
                List<?> list = (List<?>) parsed;
                if (!list.isEmpty()) {
                    sb.append(title).append("\n");
                    for (Object item : list) {
                        if (item instanceof String) {
                            sb.append("- ").append(item).append("\n");
                        } else if (item instanceof java.util.Map) {
                            // [FIX] Map을 읽기 좋은 텍스트로 변환
                            String readableText = convertMapToReadableText((java.util.Map<String, Object>) item, title);
                            sb.append("- ").append(readableText).append("\n");
                        } else {
                            sb.append("- ").append(item.toString()).append("\n");
                        }
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            // JSON 파싱 실패 시 원본 문자열 사용
            log.warn("⚠️ JSON 파싱 실패, 원본 사용: {} - {}", title, e.getMessage());
            sb.append(title).append("\n").append(jsonField).append("\n\n");
        }
    }

    /**
     * [NEW] Map을 섹션 타입에 맞게 읽기 좋은 텍스트로 변환
     */
    private String convertMapToReadableText(java.util.Map<String, Object> map, String sectionTitle) {
        StringBuilder result = new StringBuilder();

        if (sectionTitle.contains("학력")) {
            // 학력: 학교명, 전공, 기간
            appendIfPresent(result, map, "school", "schoolName", "school_name", "학교명");
            appendIfPresent(result, map, "major", "majorName", "department", "전공", "학과");
            appendIfPresent(result, map, "degree", "degreeType", "학위");
            appendIfPresent(result, map, "status", "graduationStatus", "졸업여부");
            appendIfPresent(result, map, "period", "기간");
        } else if (sectionTitle.contains("경력")) {
            // 경력: 회사명, 직책, 역할, 기간, 업무내용
            appendIfPresent(result, map, "company", "companyName", "company_name", "회사명");
            appendIfPresent(result, map, "position", "직책", "직위");
            appendIfPresent(result, map, "role", "역할", "담당업무");
            appendIfPresent(result, map, "period", "기간", "근무기간");
            appendIfPresent(result, map, "description", "업무내용", "주요업무", "key_tasks");
        } else if (sectionTitle.contains("프로젝트") || sectionTitle.contains("경험")) {
            // 프로젝트/경험: 제목, 기간, 설명
            appendIfPresent(result, map, "title", "projectName", "project_title", "name", "프로젝트명");
            appendIfPresent(result, map, "period", "기간");
            appendIfPresent(result, map, "description", "desc", "details", "내용");
        } else if (sectionTitle.contains("자격증") || sectionTitle.contains("어학")) {
            // 자격증: 이름, 취득일, 발급기관
            appendIfPresent(result, map, "title", "name", "자격증명");
            appendIfPresent(result, map, "date", "취득일", "발급일");
            appendIfPresent(result, map, "issuer", "발급기관", "기관");
        } else {
            // 기타: 모든 값을 순서대로 출력
            for (Object value : map.values()) {
                if (value != null && !value.toString().trim().isEmpty()) {
                    if (result.length() > 0) result.append(" | ");
                    result.append(value.toString().trim());
                }
            }
        }

        return result.length() > 0 ? result.toString() : map.toString();
    }

    /**
     * [NEW] Map에서 여러 키 중 하나라도 있으면 값을 추가
     */
    private void appendIfPresent(StringBuilder sb, java.util.Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(value.toString().trim());
                return; // 첫 번째 매칭된 값만 사용
            }
        }
    }

    /**
     * JSON 문자열을 Object로 파싱합니다.
     * 파싱 실패 시 원본 문자열을 단일 항목 리스트로 반환하여 데이터 손실을 방지합니다.
     */
    private Object parseJsonToObject(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            return parsed != null ? parsed : new ArrayList<>();
        } catch (JsonProcessingException e) {
            log.warn("⚠️ JSON 파싱 실패, 원본 문자열을 리스트로 반환: {}", json);
            // 파싱 실패 시 원본 문자열을 단일 항목 리스트로 반환
            return List.of(json);
        }
    }

    private void saveToDatabase(AiRecommendRequest request, AiRecommendResponse responseDto)
            throws JsonProcessingException {
        String fullJson = objectMapper.writeValueAsString(responseDto);

        ResumeAiRecommend entity = ResumeAiRecommend.builder()
                .resumeId(request.getResumeId())
                .userId(request.getUserId())
                .aiResponse(fullJson)
                .aiReport(responseDto.getAiReport())
                .build();

        ResumeAiRecommend saved = recommendRepository.save(entity);

        responseDto.setRecommendId(saved.getRecommendId());
        responseDto.setCreatedAt(saved.getCreatedAt());
    }

    /**
     * AI 추천 결과를 resume_matching 테이블에 기업별로 저장한다.
     * match_level → grade 매핑: BEST→S, HIGH→A, GAP→B
     */
    private void saveToMatchingTable(AiRecommendRequest request, AiRecommendResponse responseDto) {
        List<AiRecommendResponse.CompanyRecommend> companies = responseDto.getCompanies();
        if (companies == null || companies.isEmpty()) {
            log.warn("추천 기업이 없어 매칭 테이블 저장 생략");
            return;
        }

        String overallGradeStr = responseDto.getGrade();
        String aiReport = responseDto.getAiReport();
        int savedCount = 0;

        // 이력서 종합 등급 파싱
        ResumeMatching.Grade resumeGrade = null;
        if (overallGradeStr != null) {
            try {
                resumeGrade = ResumeMatching.Grade.valueOf(overallGradeStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        // 경력 연수 기반 주니어/시니어 판별 (3년 이상 시니어, 미만 주니어)
        ResumeMatching.ExperienceLevel expLevel = calculateExperienceLevel(request.getResumeId());

        for (AiRecommendResponse.CompanyRecommend company : companies) {
            try {
                ResumeMatching.Grade grade = mapMatchLevelToGrade(company.getMatchLevel(), overallGradeStr);

                String missingSkills = (company.getMissingSkills() != null && !company.getMissingSkills().isEmpty())
                        ? String.join(", ", company.getMissingSkills())
                        : null;

                // 이미 2-2 단계에서 매칭된 job_id, job_status 사용
                Long matchedJobId = company.getJobId() != null ? company.getJobId() : 0L;
                String matchedJobStatus = company.getJobStatus();

                ResumeMatching matching = ResumeMatching.builder()
                        .resumeId(request.getResumeId())
                        .userId(request.getUserId())
                        .jobId(matchedJobId)
                        .jobStatus(matchedJobStatus)
                        .companyName(company.getCompanyName())
                        .score(company.getMatchScore())
                        .grade(grade)
                        .resumeGrade(resumeGrade)
                        .experienceLevel(expLevel)
                        .missingSkills(missingSkills)
                        .feedback(aiReport)
                        .pros(company.getMatchLevel())
                        .cons(missingSkills)
                        .matchingType(ResumeMatching.MatchingType.AI_RECOMMEND)
                        .build();

                // 독립 트랜잭션으로 저장 (실패해도 AI 추천 응답에 영향 없음)
                matchingService.saveMatchingInNewTransaction(matching);
                savedCount++;
            } catch (Exception e) {
                log.warn("매칭 개별 저장 실패 (company: {}): {}", company.getCompanyName(), e.getMessage());
            }
        }

        log.info("매칭 테이블 저장 완료: resumeId={}, 성공={}/전체={}", request.getResumeId(), savedCount, companies.size());
    }

    /**
     * AI 추천 회사명으로 DB에서 company → job_posting을 찾아 jobId + jobStatus를 설정한다.
     * 시니어는 [경력] 공고, 주니어는 [신입] 공고 우선.
     * 해당 레벨 공고가 CLOSED이면 jobId + "CLOSED" 반환 (주니어 fallback 안 함).
     * 해당 레벨 공고 자체가 없으면 다른 ACTIVE 공고로 fallback.
     */
    private void matchJobForCompany(AiRecommendResponse.CompanyRecommend company, ResumeMatching.ExperienceLevel expLevel) {
        String companyName = company.getCompanyName();
        if (companyName == null || companyName.trim().isEmpty()) {
            company.setJobId(0L);
            company.setJobStatus(null);
            return;
        }

        try {
            // 1. 정확히 일치하는 회사 검색
            Company dbCompany = companyRepository.findByCompanyName(companyName).orElse(null);

            // 2. 없으면 한글 이름(괄호 앞부분)으로 LIKE 검색
            if (dbCompany == null) {
                String koreanName = companyName.split("[\\(（]")[0].trim();
                List<Company> candidates = companyRepository.findByCompanyNameContaining(koreanName);
                if (!candidates.isEmpty()) {
                    dbCompany = candidates.get(0);
                }
            }

            if (dbCompany == null) {
                log.debug("회사 매칭 실패: {}", companyName);
                company.setJobId(0L);
                company.setJobStatus(null);
                return;
            }

            // 3. 해당 회사의 전체 공고 (ACTIVE + CLOSED 모두)
            List<JobPosting> allJobs = jobPostingRepository
                    .findByCompanyIdOrderByCreatedAtDesc(dbCompany.getCompanyId());

            if (allJobs.isEmpty()) {
                log.debug("공고 없음: {} (companyId={})", companyName, dbCompany.getCompanyId());
                company.setJobId(0L);
                company.setJobStatus(null);
                return;
            }

            String preferKeyword = (expLevel == ResumeMatching.ExperienceLevel.SENIOR) ? "경력" : "신입";

            // 4-1. 우선 키워드 ACTIVE 공고
            for (JobPosting job : allJobs) {
                if (job.getStatus() == JobPosting.Status.ACTIVE
                        && job.getTitle() != null && job.getTitle().contains(preferKeyword)) {
                    log.info("회사-공고 매칭 (ACTIVE {}): {} → jobId={}", preferKeyword, companyName, job.getJobId());
                    company.setJobId(job.getJobId());
                    company.setJobStatus("ACTIVE");
                    return;
                }
            }

            // 4-2. 우선 키워드 CLOSED 공고 → 마감 상태로 반환 (다른 레벨로 fallback 안 함)
            for (JobPosting job : allJobs) {
                if (job.getStatus() != JobPosting.Status.ACTIVE
                        && job.getTitle() != null && job.getTitle().contains(preferKeyword)) {
                    log.info("회사-공고 매칭 (CLOSED {}): {} → jobId={}", preferKeyword, companyName, job.getJobId());
                    company.setJobId(job.getJobId());
                    company.setJobStatus("CLOSED");
                    return;
                }
            }

            // 4-3. 우선 키워드 공고 자체가 없으면 → 아무 ACTIVE 공고
            for (JobPosting job : allJobs) {
                if (job.getStatus() == JobPosting.Status.ACTIVE) {
                    log.info("회사-공고 매칭 (기본 ACTIVE): {} → jobId={}", companyName, job.getJobId());
                    company.setJobId(job.getJobId());
                    company.setJobStatus("ACTIVE");
                    return;
                }
            }

            // 5. ACTIVE 공고 없음 → 0
            log.debug("ACTIVE 공고 없음: {} (companyId={})", companyName, dbCompany.getCompanyId());
            company.setJobId(0L);
            company.setJobStatus(null);

        } catch (Exception e) {
            log.warn("회사-공고 매칭 오류 ({}): {}", companyName, e.getMessage());
            company.setJobId(0L);
            company.setJobStatus(null);
        }
    }

    /**
     * 이력서의 경력 데이터에서 총 경력 연수를 계산하여 주니어/시니어를 판별한다.
     * 3년 이상: SENIOR, 미만: JUNIOR
     */
    @SuppressWarnings("unchecked")
    private ResumeMatching.ExperienceLevel calculateExperienceLevel(Long resumeId) {
        try {
            Resume resume = resumeRepository.findById(resumeId).orElse(null);
            if (resume == null || resume.getCareers() == null || resume.getCareers().trim().isEmpty()
                    || resume.getCareers().equals("[]")) {
                log.info("경력 데이터 없음 → JUNIOR (resumeId={})", resumeId);
                return ResumeMatching.ExperienceLevel.JUNIOR;
            }

            List<?> careerList = objectMapper.readValue(resume.getCareers(), List.class);
            double totalMonths = 0;

            for (Object item : careerList) {
                if (!(item instanceof java.util.Map)) continue;
                java.util.Map<String, Object> career = (java.util.Map<String, Object>) item;

                // 형식1: period 필드 ("2022.01 - 현재 (4년)" 또는 "2019.05 - 2021.12 (36개월)")
                String period = getStringValue(career, "period");
                if (period != null) {
                    totalMonths += parseMonthsFromPeriod(period);
                    continue;
                }

                // 형식2: start_date / end_date 필드
                String startDate = getStringValue(career, "start_date", "startDate");
                String endDate = getStringValue(career, "end_date", "endDate");
                if (startDate != null) {
                    totalMonths += calculateMonthsBetween(startDate, endDate);
                }
            }

            double totalYears = totalMonths / 12.0;
            ResumeMatching.ExperienceLevel level = totalYears >= 3.0
                    ? ResumeMatching.ExperienceLevel.SENIOR
                    : ResumeMatching.ExperienceLevel.JUNIOR;

            log.info("경력 판별: resumeId={}, 총 {}개월 (약 {}년) → {}",
                    resumeId, (int) totalMonths, String.format("%.1f", totalYears), level);
            return level;

        } catch (Exception e) {
            log.warn("경력 연수 계산 실패 → JUNIOR (resumeId={}): {}", resumeId, e.getMessage());
            return ResumeMatching.ExperienceLevel.JUNIOR;
        }
    }

    private String getStringValue(java.util.Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null && !val.toString().trim().isEmpty()) {
                return val.toString().trim();
            }
        }
        return null;
    }

    /** period 문자열에서 개월 수 추출: "2022.01 - 현재 (4년)" → 48, "2022-01-15 ~ 2024-06-30" → 30 */
    private double parseMonthsFromPeriod(String period) {
        // 괄호 안의 숫자+년/개월 패턴 우선
        java.util.regex.Matcher ym = java.util.regex.Pattern.compile("\\((\\d+)년\\)").matcher(period);
        if (ym.find()) return Double.parseDouble(ym.group(1)) * 12;

        java.util.regex.Matcher mm = java.util.regex.Pattern.compile("\\((\\d+)개월\\)").matcher(period);
        if (mm.find()) return Double.parseDouble(mm.group(1));

        // ~ 구분자 우선 시도 (프론트엔드 ISO 날짜: "2022-01-15 ~ 2024-06-30")
        if (period.contains("~")) {
            String[] parts = period.split("\\s*~\\s*");
            if (parts.length == 2) {
                return calculateMonthsBetween(parts[0].trim(), parts[1].trim());
            }
        }

        // - 구분자 (DB 직접 입력: "2022.01 - 현재")
        String[] parts = period.split("\\s*-\\s*");
        if (parts.length == 2) {
            return calculateMonthsBetween(parts[0].trim(), parts[1].trim());
        }
        return 0;
    }

    /** 두 날짜 문자열 사이의 개월 수 계산 */
    private double calculateMonthsBetween(String startStr, String endStr) {
        try {
            int[] start = parseYearMonth(startStr);
            int[] end;
            if (endStr == null || endStr.contains("현재") || endStr.contains("재직")) {
                java.time.LocalDate now = java.time.LocalDate.now();
                end = new int[]{now.getYear(), now.getMonthValue()};
            } else {
                end = parseYearMonth(endStr);
            }

            if (start == null || end == null) return 0;
            return (end[0] - start[0]) * 12.0 + (end[1] - start[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    /** "2022.01", "2022-01", "2022/01" → [2022, 1] */
    private int[] parseYearMonth(String dateStr) {
        if (dateStr == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{4})[.\\-/](\\d{1,2})").matcher(dateStr);
        if (m.find()) {
            return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        }
        return null;
    }

    private ResumeMatching.Grade mapMatchLevelToGrade(String matchLevel, String overallGrade) {
        // 1. match_level 기반 매핑
        if (matchLevel != null) {
            switch (matchLevel.toUpperCase()) {
                case "BEST": return ResumeMatching.Grade.S;
                case "HIGH": return ResumeMatching.Grade.A;
                case "GAP": return ResumeMatching.Grade.B;
            }
        }
        // 2. AI 전체 등급 fallback
        if (overallGrade != null) {
            try {
                return ResumeMatching.Grade.valueOf(overallGrade.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return ResumeMatching.Grade.B;
    }

    @Transactional(readOnly = true)
    public List<AiRecommendResponse> getHistoryByUserId(Long userId) {
        List<ResumeAiRecommend> histories = recommendRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return histories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private AiRecommendResponse convertToResponse(ResumeAiRecommend entity) {
        try {
            AiRecommendResponse dto = objectMapper.readValue(entity.getAiResponse(), AiRecommendResponse.class);
            dto.setRecommendId(entity.getRecommendId());
            dto.setResumeId(entity.getResumeId() != null ? String.valueOf(entity.getResumeId()) : null);
            dto.setUserId(entity.getUserId());
            dto.setCreatedAt(entity.getCreatedAt());
            return dto;
        } catch (Exception e) {
            log.error("데이터 복구 실패: {}", e.getMessage());
            return null;
        }
    }
}