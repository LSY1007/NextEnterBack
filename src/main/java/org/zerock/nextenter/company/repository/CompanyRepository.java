package org.zerock.nextenter.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.company.entity.Company;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByBusinessNumber(String businessNumber);

    Optional<Company> findByEmailAndBusinessNumber(String email, String businessNumber);

    Optional<Company> findByCompanyName(String companyName);

    List<Company> findByCompanyNameContaining(String keyword);
}