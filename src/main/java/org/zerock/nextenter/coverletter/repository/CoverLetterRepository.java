package org.zerock.nextenter.coverletter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.coverletter.entity.CoverLetter;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {

    Page<CoverLetter> findByUserId(Long userId, Pageable pageable);

    // 수정: findByIdAndUserId → findByCoverLetterIdAndUserId
    Optional<CoverLetter> findByCoverLetterIdAndUserId(Long coverLetterId, Long userId);

    Long countByUserId(Long userId);

    List<CoverLetter> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ✅ 특정 이력서의 자기소개서 조회
    List<CoverLetter> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
}