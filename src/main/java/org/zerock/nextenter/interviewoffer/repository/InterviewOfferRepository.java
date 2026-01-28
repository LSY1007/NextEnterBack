package org.zerock.nextenter.interviewoffer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.nextenter.interviewoffer.entity.InterviewOffer;

import java.util.List;
import java.util.Optional;

public interface InterviewOfferRepository extends JpaRepository<InterviewOffer, Long> {

    /**
     * 사용자의 모든 면접 제안 조회
     */
    List<InterviewOffer> findByUserIdOrderByOfferedAtDesc(Long userId);

    /**
     * 기업의 모든 면접 제안 조회
     */
    List<InterviewOffer> findByCompanyIdOrderByOfferedAtDesc(Long companyId);

    /**
     * 특정 공고의 면접 제안 조회
     */
    List<InterviewOffer> findByJobIdOrderByOfferedAtDesc(Long jobId);

    /**
     * 중복 체크: 같은 유저에게 같은 공고로 이미 제안했는지 확인
     */
    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    /**
     * 특정 면접 제안 조회 (사용자 권한 체크용)
     */
    Optional<InterviewOffer> findByOfferIdAndUserId(Long offerId, Long userId);

    /**
     * 특정 면접 제안 조회 (기업 권한 체크용)
     */
    Optional<InterviewOffer> findByOfferIdAndCompanyId(Long offerId, Long companyId);

    /**
     * 특정 상태의 면접 제안 조회
     */
    @Query("SELECT io FROM InterviewOffer io WHERE io.userId = :userId AND io.interviewStatus = :status ORDER BY io.offeredAt DESC")
    List<InterviewOffer> findByUserIdAndStatus(
        @Param("userId") Long userId, 
        @Param("status") InterviewOffer.InterviewStatus status
    );

    /**
     * Apply와 연결된 면접 제안 조회
     */
    Optional<InterviewOffer> findByApplyId(Long applyId);
}
