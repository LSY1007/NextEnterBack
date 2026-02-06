package org.zerock.nextenter.matching.service;

import org.zerock.nextenter.matching.dto.MatchingHistoryDTO;
import org.zerock.nextenter.matching.dto.MatchingRequest;
import org.zerock.nextenter.matching.dto.MatchingResultDTO;
import org.zerock.nextenter.matching.entity.ResumeMatching;
import org.zerock.nextenter.matching.repository.ResumeMatchingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        // Grade Enum 변환
        ResumeMatching.Grade grade = ResumeMatching.Grade.valueOf(request.getGrade().toUpperCase());

        // MatchingType Enum 변환
        ResumeMatching.MatchingType matchingType = ResumeMatching.MatchingType.MANUAL;
        if (request.getMatchingType() != null) {
            matchingType = ResumeMatching.MatchingType.valueOf(request.getMatchingType().toUpperCase());
        }

        // ResumeMatching 생성
        ResumeMatching matching = ResumeMatching.builder()
                .resumeId(request.getResumeId())
                .jobId(request.getJobId())
                .grade(grade)
                .missingSkills(request.getMissingSkills())
                .cons(request.getCons())
                .feedback(request.getFeedback())
                .pros(request.getPros())
                .matchingType(matchingType)
                .build();

        ResumeMatching savedMatching = matchingRepository.save(matching);
        log.info("매칭 생성 완료: matchingId={}, resumeId={}, jobId={}",
                savedMatching.getMatchingId(), savedMatching.getResumeId(), savedMatching.getJobId());

        return convertToResultDto(savedMatching);
    }

    @Transactional(readOnly = true)
    public List<MatchingHistoryDTO> getMatchingsByResume(Long resumeId) {
        List<ResumeMatching> matchings = matchingRepository.findByResumeId(resumeId);
        return matchings.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }

    // ✅ 추가: 사용자별 전체 매칭 히스토리 조회
    @Transactional(readOnly = true)
    public List<MatchingHistoryDTO> getMatchingsByUserId(Long userId) {
        log.info("사용자 매칭 히스토리 조회 - userId: {}", userId);

        List<ResumeMatching> matchings = matchingRepository.findByUserId(userId);

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

    // Entity -> ResultDto 변환
    private MatchingResultDTO convertToResultDto(ResumeMatching matching) {
        return MatchingResultDTO.builder()
                .matchingId(matching.getMatchingId())
                .resumeId(matching.getResumeId())
                .jobId(matching.getJobId())
                .grade(matching.getGrade().name())
                .missingSkills(matching.getMissingSkills())
                .cons(matching.getCons())
                .feedback(matching.getFeedback())
                .pros(matching.getPros())
                .matchingType(matching.getMatchingType().name())
                .createdAt(matching.getCreatedAt())
                .build();
    }

    // Entity -> HistoryDto 변환
    private MatchingHistoryDTO convertToHistoryDto(ResumeMatching matching) {
        return MatchingHistoryDTO.builder()
                .matchingId(matching.getMatchingId())
                .resumeId(matching.getResumeId())
                .jobId(matching.getJobId())
                .grade(matching.getGrade().name())
                .matchingType(matching.getMatchingType().name())
                .createdAt(matching.getCreatedAt())
                .build();
    }
}