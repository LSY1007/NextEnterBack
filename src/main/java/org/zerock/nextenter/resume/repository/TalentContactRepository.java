package org.zerock.nextenter.resume.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.resume.entity.TalentContact;

import java.util.List;

@Repository
public interface TalentContactRepository extends JpaRepository<TalentContact, Long> {

    // 특정 기업 회원이 보낸 연락 요청
    List<TalentContact> findByCompanyUserIdOrderByCreatedAtDesc(Long companyUserId);

    // 특정 인재가 받은 연락 요청
    List<TalentContact> findByTalentUserIdOrderByCreatedAtDesc(Long talentUserId);

    // 특정 이력서에 대한 연락 요청
    List<TalentContact> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
}
