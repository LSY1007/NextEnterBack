package org.zerock.nextenter.interviewoffer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.nextenter.interviewoffer.entity.InterviewOffer;
import java.util.List;
import java.util.Optional;

public interface InterviewOfferRepository extends JpaRepository<InterviewOffer, Long> {

    // ✅ 서비스에서 호출하는 메서드들

    // 1. 특정 유저의 제안 목록 (날짜순) - @Where 적용됨 (삭제된 건 안 보임: 정상)
    List<InterviewOffer> findByUserIdOrderByOfferedAtDesc(Long userId);

    // 2. 기업이 보낸 제안 목록
    List<InterviewOffer> findByCompanyIdOrderByOfferedAtDesc(Long companyId);

    // 3. 공고별 제안 목록
    List<InterviewOffer> findByJobIdOrderByOfferedAtDesc(Long jobId);

    // 4. 중복 체크용
    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    // 5. 상세 조회
    Optional<InterviewOffer> findByOfferIdAndUserId(Long offerId, Long userId);
    Optional<InterviewOffer> findByOfferIdAndCompanyId(Long offerId, Long companyId);

    // 6. 상태별 조회 (JPQL 사용 -> @Where 적용됨)
    @Query("SELECT io FROM InterviewOffer io WHERE io.userId = :userId AND io.interviewStatus = :status ORDER BY io.offeredAt DESC")
    List<InterviewOffer> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") InterviewOffer.InterviewStatus status
    );

    Optional<InterviewOffer> findByApplyId(Long applyId);

    // ✅ [핵심 수정] Native Query를 사용하여 @Where(deleted=false) 필터를 무시하고 강제로 조회
    // 이렇게 해야 '휴지통(삭제된 제안)' 조회가 가능합니다.
    @Query(value = "SELECT * FROM interview_offer WHERE user_id = :userId AND deleted = :deleted ORDER BY offered_at DESC", nativeQuery = true)
    List<InterviewOffer> findByUserIdAndDeletedOrderByOfferedAtDesc(
            @Param("userId") Long userId,
            @Param("deleted") boolean deleted
    );

    // ✅ 기업+유저 조회
    List<InterviewOffer> findByCompanyIdAndUserId(Long companyId, Long userId);
}