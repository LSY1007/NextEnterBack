package org.zerock.nextenter.interviewoffer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.nextenter.company.entity.Company;
import org.zerock.nextenter.company.repository.CompanyRepository;
import org.zerock.nextenter.apply.entity.Apply;
import org.zerock.nextenter.apply.repository.ApplyRepository;
import org.zerock.nextenter.interviewoffer.dto.InterviewOfferRequest;
import org.zerock.nextenter.interviewoffer.dto.InterviewOfferResponse;
import org.zerock.nextenter.interviewoffer.entity.InterviewOffer;
import org.zerock.nextenter.interviewoffer.repository.InterviewOfferRepository;
import org.zerock.nextenter.job.entity.JobPosting;
import org.zerock.nextenter.job.repository.JobPostingRepository;
import org.zerock.nextenter.user.entity.User;
import org.zerock.nextenter.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InterviewOfferService {

    private final InterviewOfferRepository interviewOfferRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ApplyRepository applyRepository;

    @Autowired(required = false)
    private org.zerock.nextenter.notification.NotificationService notificationService;

    // 제안 생성
    @Transactional
    public InterviewOfferResponse createOffer(Long companyId, InterviewOfferRequest request) {
        if (interviewOfferRepository.existsByUserIdAndJobId(request.getUserId(), request.getJobId())) {
            throw new IllegalStateException("이미 면접 제안한 지원자입니다");
        }
        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));
        if (!job.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("해당 공고의 기업이 아닙니다");
        }
        InterviewOffer offer = InterviewOffer.builder()
                .userId(request.getUserId())
                .jobId(request.getJobId())
                .companyId(companyId)
                .applyId(request.getApplyId())
                .offerType(request.getApplyId() != null ? InterviewOffer.OfferType.FROM_APPLICATION : InterviewOffer.OfferType.COMPANY_INITIATED)
                .interviewStatus(InterviewOffer.InterviewStatus.OFFERED)
                .build();
        offer = interviewOfferRepository.save(offer);

        // ✅ applyId가 있으면 (지원자 목록에서 제안) Apply 상태를 서류합격으로 업데이트
        if (request.getApplyId() != null) {
            Apply apply = applyRepository.findById(request.getApplyId())
                    .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다"));
            apply.setDocumentStatus(Apply.DocumentStatus.PASSED);
            apply.setReviewedAt(LocalDateTime.now());
            applyRepository.save(apply);
            log.info("✅ 지원자 목록에서 면접 제안 → Apply documentStatus를 PASSED로 업데이트: applyId={}", request.getApplyId());
        }

        if (notificationService != null) {
            try {
                Company company = companyRepository.findById(companyId).orElse(null);
                String companyName = company != null ? company.getCompanyName() : "기업";
                notificationService.notifyInterviewOffer(request.getUserId(), companyName, job.getTitle(), offer.getOfferId(), null);
            } catch (Exception e) { log.error("알림 실패", e); }
        }
        return convertToResponse(offer);
    }

    // 제안 수락
    @Transactional
    public InterviewOfferResponse acceptOffer(Long offerId, Long userId) {
        InterviewOffer offer = interviewOfferRepository.findByOfferIdAndUserId(offerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제안을 찾을 수 없습니다"));
        if (offer.getInterviewStatus() != InterviewOffer.InterviewStatus.OFFERED) {
            throw new IllegalStateException("수락 불가 상태");
        }
        offer.setInterviewStatus(InterviewOffer.InterviewStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        interviewOfferRepository.save(offer);
        // 알림 로직 생략(간소화)
        return convertToResponse(offer);
    }

    // 제안 거절
    @Transactional
    public InterviewOfferResponse rejectOffer(Long offerId, Long userId) {
        InterviewOffer offer = interviewOfferRepository.findByOfferIdAndUserId(offerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제안을 찾을 수 없습니다"));
        if (offer.getInterviewStatus() == InterviewOffer.InterviewStatus.OFFERED) {
            offer.setInterviewStatus(InterviewOffer.InterviewStatus.REJECTED);
            offer.setFinalResult(InterviewOffer.FinalResult.REJECTED);
        } else {
            offer.setInterviewStatus(InterviewOffer.InterviewStatus.CANCELED);
        }
        offer.setRespondedAt(LocalDateTime.now());
        interviewOfferRepository.save(offer);
        return convertToResponse(offer);
    }

    // 단일 삭제
    @Transactional
    public void deleteOffer(Long offerId, Long userId) {
        InterviewOffer offer = interviewOfferRepository.findByOfferIdAndUserId(offerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("제안을 찾을 수 없습니다"));
        offer.setDeleted(true);
        offer.setDeletedAt(LocalDateTime.now());
    }

    // ✅ [추가] 일괄 삭제
    @Transactional
    public void deleteOffers(List<Long> offerIds, Long userId) {
        List<InterviewOffer> offers = interviewOfferRepository.findAllById(offerIds);
        for (InterviewOffer offer : offers) {
            if (!offer.getUserId().equals(userId)) continue; // 내 거 아니면 스킵
            offer.setDeleted(true);
            offer.setDeletedAt(LocalDateTime.now());
        }
    }

    // 조회 메서드들
    public List<InterviewOfferResponse> getReceivedOffers(Long userId) {
        return interviewOfferRepository.findByUserIdAndStatus(userId, InterviewOffer.InterviewStatus.OFFERED)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<InterviewOfferResponse> getMyOffers(Long userId, Boolean includeDeleted) {
        List<InterviewOffer> offers;
        if (includeDeleted != null && includeDeleted) {
            offers = interviewOfferRepository.findByUserIdAndDeletedOrderByOfferedAtDesc(userId, true);
        } else {
            offers = interviewOfferRepository.findByUserIdOrderByOfferedAtDesc(userId);
        }
        return offers.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<InterviewOfferResponse> getCompanyOffers(Long companyId, Long jobId) {
        List<InterviewOffer> offers = (jobId != null) ?
                interviewOfferRepository.findByJobIdOrderByOfferedAtDesc(jobId) :
                interviewOfferRepository.findByCompanyIdOrderByOfferedAtDesc(companyId);
        return offers.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<Long> getOfferedJobIds(Long companyId, Long userId) {
        return interviewOfferRepository.findByCompanyIdAndUserId(companyId, userId)
                .stream().map(InterviewOffer::getJobId).distinct().collect(Collectors.toList());
    }

    private InterviewOfferResponse convertToResponse(InterviewOffer offer) {
        JobPosting job = jobPostingRepository.findById(offer.getJobId()).orElse(null);
        User user = userRepository.findById(offer.getUserId()).orElse(null);
        Company company = companyRepository.findById(offer.getCompanyId()).orElse(null);

        return InterviewOfferResponse.builder()
                .offerId(offer.getOfferId())
                .userId(offer.getUserId())
                .jobId(offer.getJobId())
                .companyId(offer.getCompanyId())
                .applyId(offer.getApplyId())
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .companyName(company != null ? company.getCompanyName() : "알 수 없음")
                .deadline(job != null ? job.getDeadline() : null)
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .offerType(offer.getOfferType().name())
                .interviewStatus(offer.getInterviewStatus().name())
                .finalResult(offer.getFinalResult() != null ? offer.getFinalResult().name() : null)
                .deleted(offer.getDeleted())
                .offeredAt(offer.getOfferedAt())
                .respondedAt(offer.getRespondedAt())
                .scheduledAt(offer.getScheduledAt())
                .updatedAt(offer.getUpdatedAt())
                .deletedAt(offer.getDeletedAt())
                .build();
    }
}