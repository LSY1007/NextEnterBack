package org.zerock.nextenter.resume.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.resume.entity.SavedTalent;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedTalentRepository extends JpaRepository<SavedTalent, Long> {

    // 특정 기업 회원이 특정 이력서를 저장했는지 확인
    Optional<SavedTalent> findByCompanyUserIdAndResumeId(Long companyUserId, Long resumeId);

    // 특정 기업 회원이 저장한 모든 인재
    List<SavedTalent> findByCompanyUserIdOrderByCreatedAtDesc(Long companyUserId);

    // 특정 이력서가 저장된 횟수
    long countByResumeId(Long resumeId);
}
