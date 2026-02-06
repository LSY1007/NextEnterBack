package org.zerock.nextenter.matching.service;

import org.zerock.nextenter.matching.dto.MatchingHistoryDTO;
import org.zerock.nextenter.matching.dto.MatchingRequest;
import org.zerock.nextenter.matching.dto.MatchingResultDTO;
import org.zerock.nextenter.matching.entity.ResumeMatching;
import org.zerock.nextenter.matching.repository.ResumeMatchingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final ResumeMatchingRepository matchingRepository;

    @Transactional
    public MatchingResultDTO createMatching(MatchingRequest request) {
        ResumeMatching.Grade grade = ResumeMatching.Grade.valueOf(request.getGrade().toUpperCase());

        ResumeMatching.MatchingType matchingType = ResumeMatching.MatchingType.MANUAL;
        if (request.getMatchingType() != null) {
            matchingType = ResumeMatching.MatchingType.valueOf(request.getMatchingType().toUpperCase());
        }

        ResumeMatching matching = ResumeMatching.builder()
                .resumeId(request.getResumeId())
                .userId(request.getUserId())
                .jobId(request.getJobId())
                .companyName(request.getCompanyName())
                .score(request.getScore())
                .grade(grade)
                .missingSkills(request.getMissingSkills())
                .cons(request.getCons())
                .feedback(request.getFeedback())
                .pros(request.getPros())
                .matchingType(matchingType)
                .build();

        ResumeMatching savedMatching = matchingRepository.save(matching);
        log.info("매칭 생성 완료: matchingId={}, resumeId={}, company={}",
                savedMatching.getMatchingId(), savedMatching.getResumeId(), savedMatching.getCompanyName());

        return convertToResultDto(savedMatching);
    }

    /**
     * 독립 트랜잭션으로 매칭 저장 (AI 추천 서비스에서 호출)
     * 실패해도 호출자 트랜잭션에 영향 없음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMatchingInNewTransaction(ResumeMatching matching) {
        matchingRepository.save(matching);
        log.info("매칭 저장 완료 (독립 트랜잭션): resumeId={}, company={}",
                matching.getResumeId(), matching.getCompanyName());
    }

    @Transactional(readOnly = true)
    public List<MatchingHistoryDTO> getMatchingsByResume(Long resumeId) {
        List<ResumeMatching> matchings = matchingRepository.findByResumeId(resumeId);
        return matchings.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchingHistoryDTO> getMatchingsByUserId(Long userId) {
        log.info("사용자 매칭 히스토리 조회 - userId: {}", userId);
        List<ResumeMatching> matchings = matchingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("사용자 매칭 히스토리 조회 완료 - userId: {}, 매칭 수: {}", userId, matchings.size());
        return matchings.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchingResultDTO> getMatchingsByJob(Long jobId) {
        List<ResumeMatching> matchings = matchingRepository.findByJobId(jobId);
        return matchings.stream()
                .map(this::convertToResultDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchingResultDTO> getMatchingsByJobAndGrade(Long jobId, String grade) {
        ResumeMatching.Grade gradeEnum = ResumeMatching.Grade.valueOf(grade.toUpperCase());
        List<ResumeMatching> matchings = matchingRepository.findByJobIdAndGrade(jobId, gradeEnum);
        return matchings.stream()
                .map(this::convertToResultDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatchingResultDTO getMatchingById(Long matchingId) {
        ResumeMatching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));
        return convertToResultDto(matching);
    }

    @Transactional
    public void deleteMatching(Long matchingId) {
        ResumeMatching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매칭입니다."));
        matchingRepository.delete(matching);
        log.info("매칭 삭제: matchingId={}", matchingId);
    }

    private MatchingResultDTO convertToResultDto(ResumeMatching matching) {
        return MatchingResultDTO.builder()
                .matchingId(matching.getMatchingId())
                .resumeId(matching.getResumeId())
                .userId(matching.getUserId())
                .jobId(matching.getJobId())
                .companyName(matching.getCompanyName())
                .score(matching.getScore())
                .grade(matching.getGrade().name())
                .missingSkills(matching.getMissingSkills())
                .cons(matching.getCons())
                .feedback(matching.getFeedback())
                .pros(matching.getPros())
                .matchingType(matching.getMatchingType().name())
                .createdAt(matching.getCreatedAt())
                .build();
    }

    private MatchingHistoryDTO convertToHistoryDto(ResumeMatching matching) {
        return MatchingHistoryDTO.builder()
                .matchingId(matching.getMatchingId())
                .resumeId(matching.getResumeId())
                .userId(matching.getUserId())
                .jobId(matching.getJobId())
                .jobStatus(matching.getJobStatus())
                .companyName(matching.getCompanyName())
                .score(matching.getScore())
                .grade(matching.getGrade().name())
                .resumeGrade(matching.getResumeGrade() != null ? matching.getResumeGrade().name() : null)
                .experienceLevel(matching.getExperienceLevel() != null ? matching.getExperienceLevel().name() : null)
                .missingSkills(matching.getMissingSkills())
                .feedback(matching.getFeedback())
                .pros(matching.getPros())
                .cons(matching.getCons())
                .matchingType(matching.getMatchingType().name())
                .createdAt(matching.getCreatedAt())
                .build();
    }
}
