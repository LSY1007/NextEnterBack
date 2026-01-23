package org.zerock.nextenter.resume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.resume.dto.TalentSearchResponse;
import org.zerock.nextenter.resume.entity.Resume;
import org.zerock.nextenter.resume.entity.SavedTalent;
import org.zerock.nextenter.resume.entity.TalentContact;
import org.zerock.nextenter.resume.repository.ResumeRepository;
import org.zerock.nextenter.resume.repository.SavedTalentRepository;
import org.zerock.nextenter.resume.repository.TalentContactRepository;
import org.zerock.nextenter.notification.NotificationService;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TalentService {

    private final SavedTalentRepository savedTalentRepository;
    private final TalentContactRepository talentContactRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 스크랩한 인재 목록 조회
     */
    public Page<TalentSearchResponse> getSavedTalents(Long companyUserId) {
        log.info("스크랩한 인재 목록 조회 - companyUserId: {}", companyUserId);

        // 저장된 인재 목록 조회
        List<SavedTalent> savedTalents = savedTalentRepository
                .findByCompanyUserIdOrderByCreatedAtDesc(companyUserId);

        log.info("저장된 인재 수: {}", savedTalents.size());

        // 이력서 정보를 가져와서 TalentSearchResponse로 변환
        List<TalentSearchResponse> responses = new ArrayList<>();
        
        for (SavedTalent savedTalent : savedTalents) {
            try {
                Resume resume = resumeRepository.findById(savedTalent.getResumeId())
                        .orElse(null);
                
                if (resume == null || resume.getVisibility() != Resume.Visibility.PUBLIC) {
                    log.info("이력서가 삭제되었거나 비공개 상태입니다 - resumeId: {}", savedTalent.getResumeId());
                    continue;
                }

                // 스킬 파싱
                List<String> skills = new ArrayList<>();
                if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
                    try {
                        // JSON 배열 파싱
                        String skillsJson = resume.getSkills();
                        skillsJson = skillsJson.replaceAll("[\\[\\]\"]", "");
                        String[] skillArray = skillsJson.split(",");
                        for (String skill : skillArray) {
                            skills.add(skill.trim());
                        }
                    } catch (Exception e) {
                        log.error("스킬 파싱 오류", e);
                    }
                }

                // 가명 처리
                String maskedName = resume.getUserId() != null ? 
                        "김*연" : "이름 비공개";

                TalentSearchResponse response = TalentSearchResponse.builder()
                        .resumeId(resume.getResumeId())
                        .userId(resume.getUserId())
                        .name(maskedName)
                        .jobCategory(resume.getJobCategory())
                        .skills(skills)
                        .location("서울특별시") // 기본값
                        .experienceYears(0) // 기본값
                        .salaryRange("협의") // 기본값
                        .matchScore(80) // 기본값
                        .isAvailable(true)
                        .viewCount(resume.getViewCount())
                        .build();

                responses.add(response);
            } catch (Exception e) {
                log.error("이력서 처리 중 오류 - resumeId: {}", savedTalent.getResumeId(), e);
            }
        }

        log.info("최종 반환 인재 수: {}", responses.size());
        return new PageImpl<>(responses);
    }

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

        contact = talentContactRepository.save(contact);
        log.info("연락 요청 완료 - contactId: {}", contact.getContactId());
        
        // 인재에게 연락 알림 전송
        try {
            // 기업 사용자 정보에서 회사명 가져오기
            User companyUser = userRepository.findById(companyUserId).orElse(null);
            String companyName = companyUser != null && companyUser.getName() != null 
                ? companyUser.getName() : "기업";
            
            String jobTitle = resume.getJobCategory() != null 
                ? resume.getJobCategory() : "포지션";
            
            log.info("알림 전송 시도 - talentUserId: {}, companyName: {}, jobTitle: {}", 
                resume.getUserId(), companyName, jobTitle);
            
            notificationService.notifyInterviewOffer(
                resume.getUserId(),
                companyName,
                jobTitle,
                contact.getContactId()
            );
            
            log.info("인재 연락 알림 전송 성공!");
        } catch (Exception e) {
            log.error("인재 연락 알림 전송 실패", e);
            // 알림 실패해도 연락은 저장됨
        }
        
        return contact;
    }

    /**
     * 인재가 받은 연락 메시지 조회
     */
    public List<TalentContact> getReceivedContacts(Long talentUserId) {
        log.info("인재가 받은 연락 메시지 조회 - talentUserId: {}", talentUserId);
        return talentContactRepository.findByTalentUserIdOrderByCreatedAtDesc(talentUserId);
    }

    /**
     * 연락 메시지 상태 변경
     */
    @Transactional
    public boolean updateContactStatus(Long contactId, String status, Long talentUserId) {
        log.info("연락 메시지 상태 변경 - contactId: {}, status: {}, talentUserId: {}", 
                contactId, status, talentUserId);

        TalentContact contact = talentContactRepository.findById(contactId)
                .orElse(null);

        if (contact == null) {
            log.warn("연락 메시지를 찾을 수 없음 - contactId: {}", contactId);
            return false;
        }

        // 권한 확인: 해당 인재가 받은 메시지인지 확인
        if (!contact.getTalentUserId().equals(talentUserId)) {
            log.warn("권한 없음 - talentUserId: {} != {}", talentUserId, contact.getTalentUserId());
            return false;
        }

        contact.setStatus(status);
        talentContactRepository.save(contact);
        log.info("연락 메시지 상태 변경 완료 - new status: {}", status);
        
        // ✅ 기업에게 알림 전송
        try {
            // 인재 정보 가져오기
            User talentUser = userRepository.findById(talentUserId).orElse(null);
            String talentName = talentUser != null && talentUser.getName() != null 
                ? talentUser.getName() : "인재";
            
            // 상태에 따른 알림 메시지
            String statusText = status.equals("ACCEPTED") ? "수락" : "거절";
            
            log.info("알림 전송 시도 - companyUserId: {}, talentName: {}, status: {}", 
                contact.getCompanyUserId(), talentName, statusText);
            
            notificationService.notifyContactResponse(
                contact.getCompanyUserId(),
                talentName,
                statusText,
                contactId
            );
            
            log.info("연락 제안 응답 알림 전송 성공!");
        } catch (Exception e) {
            log.error("연락 제안 응답 알림 전송 실패", e);
            // 알림 실패해도 상태 변경은 저장됨
        }
        
        return true;
    }

    /**
     * 기업이 특정 인재에게 보낸 가장 최근 연락 조회
     */
    public TalentContact getLatestContact(Long companyUserId, Long resumeId) {
        log.info("최신 연락 메시지 조회 - companyUserId: {}, resumeId: {}", companyUserId, resumeId);
        
        List<TalentContact> contacts = talentContactRepository
                .findByResumeIdOrderByCreatedAtDesc(resumeId);
        
        // 해당 기업이 보낸 연락 중 가장 최근 것 반환
        for (TalentContact contact : contacts) {
            if (contact.getCompanyUserId().equals(companyUserId)) {
                return contact;
            }
        }
        
        return null;
    }
}
