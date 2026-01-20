package org.zerock.nextenter.apply.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.apply.entity.Apply;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {

    // 특정 공고의 모든 지원자 조회
    @Query("SELECT a FROM Apply a WHERE a.jobId = :jobId ORDER BY a.appliedAt DESC")
    List<Apply> findByJobId(@Param("jobId") Long jobId);

    // 특정 기업의 모든 지원자 조회 (공고를 통해)
    @Query("SELECT a FROM Apply a " +
            "JOIN JobPosting j ON a.jobId = j.jobId " +
            "WHERE j.companyId = :companyId " +
            "ORDER BY a.appliedAt DESC")
    Page<Apply> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    // 특정 공고의 지원자 조회 (페이징)
    @Query("SELECT a FROM Apply a WHERE a.jobId = :jobId ORDER BY a.appliedAt DESC")
    Page<Apply> findByJobIdPaged(@Param("jobId") Long jobId, Pageable pageable);

    // 지원자 상세 조회 (기업 소유 확인 포함)
    @Query("SELECT a FROM Apply a " +
            "JOIN JobPosting j ON a.jobId = j.jobId " +
            "WHERE a.applyId = :applyId AND j.companyId = :companyId")
    Optional<Apply> findByIdAndCompanyId(
            @Param("applyId") Long applyId,
            @Param("companyId") Long companyId
    );

    // 사용자의 특정 공고 지원 여부 확인
    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    // 사용자의 모든 지원 내역
    List<Apply> findByUserIdOrderByAppliedAtDesc(Long userId);

    // 상태별 지원자 수
    @Query("SELECT COUNT(a) FROM Apply a " +
            "JOIN JobPosting j ON a.jobId = j.jobId " +
            "WHERE j.companyId = :companyId AND a.status = :status")
    Long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") Apply.Status status);
    
    // 특정 공고의 지원자 수
    @Query("SELECT COUNT(a) FROM Apply a WHERE a.jobId = :jobId")
    Long countByJobId(@Param("jobId") Long jobId);
}