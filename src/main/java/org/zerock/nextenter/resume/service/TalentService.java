package org.zerock.nextenter.resume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.entity.SavedTalent;
import org.zerock.nextenter.resume.entity.TalentContact;
import org.zerock.nextenter.resume.repository.ResumeRepository;
import org.zerock.nextenter.resume.repository.SavedTalentRepository;
import org.zerock.nextenter.resume.repository.TalentContactRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TalentService {

    private final SavedTalentRepository savedTalentRepository;
    private final TalentContactRepository talentContactRepository;
    private final ResumeRepository resumeRepository;

    /**
     * 인재 저장 (북마크)
     */
    @Transactional
    public boolean saveTalent(Long companyUserId, Long resumeId) {
        log.info("인재 저장 - companyUserId: {}, resumeId: {}", companyUserId, resumeId);

        // 이미 저장되어 있는지 확인
        Optional<SavedTalent> existing = savedTalentRepository
                .findByCompanyUserIdAndResumeId(companyUserId, resumeId);

        if (existing.isPresent()) {
            log.info("이미 저장된 인재입니다.");
            return false;
        }

        // 이력서가 존재하고 공개 상태인지 확인
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (resume.getVisibility() != Resume.Visibility.PUBLIC) {
            throw new IllegalArgumentException("비공개 이력서는 저장할 수 없습니다");
        }

        SavedTalent savedTalent = SavedTalent.builder()
                .companyUserId(companyUserId)
                .resumeId(resumeId)
                .build();

        savedTalentRepository.save(savedTalent);
        log.info("인재 저장 완료");
        return true;
    }

    /**
     * 인재 저장 취소
     */
    @Transactional
    public boolean unsaveTalent(Long companyUserId, Long resumeId) {
        log.info("인재 저장 취소 - companyUserId: {}, resumeId: {}", companyUserId, resumeId);

        Optional<SavedTalent> savedTalent = savedTalentRepository
                .findByCompanyUserIdAndResumeId(companyUserId, resumeId);

        if (savedTalent.isEmpty()) {
            log.info("저장되지 않은 인재입니다.");
            return false;
        }

        savedTalentRepository.delete(savedTalent.get());
        log.info("인재 저장 취소 완료");
        return true;
    }

    /**
     * 인재 저장 여부 확인
     */
    public boolean isSaved(Long companyUserId, Long resumeId) {
        return savedTalentRepository
                .findByCompanyUserIdAndResumeId(companyUserId, resumeId)
                .isPresent();
    }

    /**
     * 인재 연락하기
     */
    @Transactional
    public TalentContact contactTalent(Long companyUserId, Long resumeId, String message) {
        log.info("인재 연락 - companyUserId: {}, resumeId: {}", companyUserId, resumeId);

        // 이력서가 존재하고 공개 상태인지 확인
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다"));

        if (resume.getVisibility() != Resume.Visibility.PUBLIC) {
            throw new IllegalArgumentException("비공개 이력서에는 연락할 수 없습니다");
        }

        TalentContact contact = TalentContact.builder()
                .companyUserId(companyUserId)
                .resumeId(resumeId)
                .talentUserId(resume.getUserId())
                .message(message)
                .status("PENDING")
                .build();

        talentContactRepository.save(contact);
        log.info("연락 요청 완료");
        return contact;
    }
}
