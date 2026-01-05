package org.zerock.nextenter.company.repository;

import org.zerock.nextenter.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUser_UserId(Long userId);

    boolean existsByBusinessNumber(String businessNumber);
}