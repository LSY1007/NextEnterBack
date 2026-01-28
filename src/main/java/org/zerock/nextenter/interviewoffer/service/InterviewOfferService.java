package org.zerock.nextenter.interviewoffer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final org.zerock.nextenter.notification.NotificationService notificationService;

    /**
     * 기업이 인재검색에서 면접 제안
     */
    @Transactional
    public InterviewOfferResponse createOffer(Long companyId, InterviewOfferRequest request) {
        log.info("면접 제안 생성 - companyId: {}, userId: {}, jobId: {}",
                companyId, request.getUserId(), request.getJobId());

        // 중복 체크
        if (interviewOfferRepository.existsByUserIdAndJobId(request.getUserId(), request.getJobId())) {
            throw new IllegalStateException("이미 면접 제안한 지원자입니다");
        }

        // 공고 검증
        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다"));

        if (!job.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("해당 공고의 기업이 아닙니다");
        }

        // 면접 제안 생성
        InterviewOffer offer = InterviewOffer.builder()
                .userId(request.getUserId())
                .jobId(request.getJobId())
                .companyId(companyId)
                .applyId(request.getApplyId())
                .offerType(request.getApplyId() != null ?
                        InterviewOffer.OfferType.FROM_APPLICATION :
                        InterviewOffer.OfferType.COMPANY_INITIATED)
                .interviewStatus(InterviewOffer.InterviewStatus.OFFERED)
                .build();

        offer = interviewOfferRepository.save(offer);

        // 알림 전송
        try {
            notificationService.notifyApplicationStatus(
                    request.getUserId(),
                    job.getTitle(),
                    "면접 제안",
                    offer.getOfferId()
            );
        } catch (Exception e) {
            log.error("면접 제안 알림 전송 실패", e);
        }

        return convertToResponse(offer);
    }

    /**
     * 면접 제안 수락
     */
    @Transactional
    public InterviewOfferResponse acceptOffer(Long offerId, Long userId) {
        log.info("면접 제안 수락 - offerId: {}, userId: {}", offerId, userId);

        InterviewOffer offer = interviewOfferRepository.findByOfferIdAndUserId(offerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("면접 제안을 찾을 수 없습니다"));

        if (offer.getInterviewStatus() != InterviewOffer.InterviewStatus.OFFERED) {
            throw new IllegalStateException("수락할 수 없는 상태입니다");
        }

        offer.setInterviewStatus(InterviewOffer.InterviewStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());

        interviewOfferRepository.save(offer);

        // 기업에 알림
        try {
            notificationService.notifyApplicationStatus(
                    offer.getCompanyId(),
                    "면접 수락",
                    "지원자가 면접을 수락했습니다",
                    offerId
            );
        } catch (Exception e) {
            log.error("면접 수락 알림 전송 실패", e);
        }

        return convertToResponse(offer);
    }

    /**
     * 면접 제안 거절 (또는 취소)
     * ✅ 수정됨: OFFERED 상태뿐만 아니라 ACCEPTED 상태에서도 취소 가능하도록 변경
     */
    @Transactional
    public InterviewOfferResponse rejectOffer(Long offerId, Long userId) {
        log.info("면접 제안 취소/거절 - offerId: {}, userId: {}", offerId, userId);

        InterviewOffer offer = interviewOfferRepository.findByOfferIdAndUserId(offerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("면접 제안을 찾을 수 없습니다"));

        // ✅ [핵심 수정] OFFERED(제안됨) 또는 ACCEPTED(수락함) 상태일 때만 취소/거절 가능
        if (offer.getInterviewStatus() != InterviewOffer.InterviewStatus.OFFERED &&
                offer.getInterviewStatus() != InterviewOffer.InterviewStatus.ACCEPTED) {
            throw new IllegalStateException("취소하거나 거절할 수 없는 상태입니다 (현재 상태: " + offer.getInterviewStatus() + ")");
        }

        // 상태 분기 처리
        if (offer.getInterviewStatus() == InterviewOffer.InterviewStatus.OFFERED) {
            // 1. 아직 수락 안 했을 때 -> '거절(REJECTED)' 처리
            offer.setInterviewStatus(InterviewOffer.InterviewStatus.REJECTED);
            offer.setFinalResult(InterviewOffer.FinalResult.REJECTED);
        } else {
            // 2. 이미 수락 했을 때 -> '취소(CANCELED)' 처리
            // (FinalResult는 '불합격'이 아니므로 건드리지 않거나 null 유지)
            offer.setInterviewStatus(InterviewOffer.InterviewStatus.CANCELED);
        }

        offer.setRespondedAt(LocalDateTime.now());

        interviewOfferRepository.save(offer);

        return convertToResponse(offer);
    }

    /**
     * 사용자의 받은 제안 목록 (OFFERED 상태만)
     */
    public List<InterviewOfferResponse> getReceivedOffers(Long userId) {
        log.info("받은 제안 조회 - userId: {}", userId);

        List<InterviewOffer> offers = interviewOfferRepository.findByUserIdAndStatus(
                userId, InterviewOffer.InterviewStatus.OFFERED);

        return offers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 모든 면접 제안 조회
     */
    public List<InterviewOfferResponse> getMyOffers(Long userId) {
        log.info("내 면접 제안 조회 - userId: {}", userId);

        List<InterviewOffer> offers = interviewOfferRepository.findByUserIdOrderByOfferedAtDesc(userId);

        return offers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 기업의 면접 제안 목록
     */
    public List<InterviewOfferResponse> getCompanyOffers(Long companyId, Long jobId) {
        log.info("기업 면접 제안 조회 - companyId: {}, jobId: {}", companyId, jobId);

        List<InterviewOffer> offers = jobId != null ?
                interviewOfferRepository.findByJobIdOrderByOfferedAtDesc(jobId) :
                interviewOfferRepository.findByCompanyIdOrderByOfferedAtDesc(companyId);

        return offers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * DTO 변환
     */
    private InterviewOfferResponse convertToResponse(InterviewOffer offer) {
        JobPosting job = jobPostingRepository.findById(offer.getJobId()).orElse(null);
        User user = userRepository.findById(offer.getUserId()).orElse(null);
        User company = userRepository.findById(offer.getCompanyId()).orElse(null);

        return InterviewOfferResponse.builder()
                .offerId(offer.getOfferId())
                .userId(offer.getUserId())
                .jobId(offer.getJobId())
                .companyId(offer.getCompanyId())
                .applyId(offer.getApplyId())
                .jobTitle(job != null ? job.getTitle() : "알 수 없음")
                .jobCategory(job != null ? job.getJobCategory() : "알 수 없음")
                .companyName(company != null ? company.getName() : "알 수 없음")
                .userName(user != null ? user.getName() : "알 수 없음")
                .userAge(user != null ? user.getAge() : null)
                .offerType(offer.getOfferType().name())
                .interviewStatus(offer.getInterviewStatus().name())
                .finalResult(offer.getFinalResult() != null ? offer.getFinalResult().name() : null)
                .offeredAt(offer.getOfferedAt())
                .respondedAt(offer.getRespondedAt())
                .scheduledAt(offer.getScheduledAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}