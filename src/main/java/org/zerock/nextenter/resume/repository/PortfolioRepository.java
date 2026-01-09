package org.zerock.nextenter.resume.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.resume.entity.Portfolio;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    @Query("SELECT p FROM Portfolio p WHERE p.resume.resumeId = :resumeId ORDER BY p.displayOrder ASC, p.createdAt DESC")
    List<Portfolio> findByResumeIdOrderByDisplayOrder(@Param("resumeId") Long resumeId);

    @Query("SELECT p FROM Portfolio p WHERE p.portfolioId = :portfolioId AND p.resume.resumeId = :resumeId")
    Optional<Portfolio> findByIdAndResumeId(@Param("portfolioId") Long portfolioId, @Param("resumeId") Long resumeId);

    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.resume.resumeId = :resumeId")
    Long countByResumeId(@Param("resumeId") Long resumeId);

    @Query("SELECT SUM(p.fileSize) FROM Portfolio p WHERE p.resume.resumeId = :resumeId")
    Long sumFileSizeByResumeId(@Param("resumeId") Long resumeId);

    void deleteByResumeResumeId(Long resumeId);
}