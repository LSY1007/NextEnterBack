package org.zerock.nextenter.advertisement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.nextenter.advertisement.entity.Advertisement;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    
    // 활성화된 광고만 우선순위 순으로 조회
    List<Advertisement> findByIsActiveTrueOrderByPriorityDescCreatedAtDesc();
    
    // 특정 기업의 광고 조회
    List<Advertisement> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    
    // 특정 기업의 활성화된 광고 조회
    List<Advertisement> findByCompanyIdAndIsActiveTrueOrderByPriorityDesc(Long companyId);
}
