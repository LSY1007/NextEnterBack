package org.zerock.nextenter.job.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.zerock.nextenter.job.entity.JobPosting;

import java.util.List;

@Slf4j
public class JobPostingRepositoryCustomImpl implements JobPostingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<JobPosting> searchByFiltersWithRegionLike(
            List<String> categoryList,
            List<String> regionList,
            String keyword,
            JobPosting.Status statusEnum,
            Pageable pageable) {

        // JPQL 쿼리 동적 생성
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT j FROM JobPosting j WHERE 1=1 ");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(DISTINCT j) FROM JobPosting j WHERE 1=1 ");

        // 직무 카테고리 필터
        if (categoryList != null && !categoryList.isEmpty()) {
            jpql.append("AND j.jobCategory IN :categoryList ");
            countJpql.append("AND j.jobCategory IN :categoryList ");
        }

        // 지역 필터 (LIKE 검색)
        if (regionList != null && !regionList.isEmpty()) {
            jpql.append("AND (");
            countJpql.append("AND (");
            for (int i = 0; i < regionList.size(); i++) {
                if (i > 0) {
                    jpql.append(" OR ");
                    countJpql.append(" OR ");
                }
                jpql.append("(j.locationCity LIKE :region").append(i)
                    .append(" OR j.location LIKE :region").append(i).append(")");
                countJpql.append("(j.locationCity LIKE :region").append(i)
                    .append(" OR j.location LIKE :region").append(i).append(")");
            }
            jpql.append(") ");
            countJpql.append(") ");
        }

        // 키워드 검색
        if (keyword != null && !keyword.isEmpty()) {
            jpql.append("AND (j.title LIKE :keyword OR j.description LIKE :keyword) ");
            countJpql.append("AND (j.title LIKE :keyword OR j.description LIKE :keyword) ");
        }

        // 상태 필터 (null이면 전체 상태 조회)
        if (statusEnum != null) {
            jpql.append("AND j.status = :status ");
            countJpql.append("AND j.status = :status ");
        }

        // 정렬
        jpql.append("ORDER BY j.createdAt DESC");

        log.debug("Generated JPQL: {}", jpql.toString());

        // Count 쿼리 생성
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        
        // 메인 쿼리 생성
        TypedQuery<JobPosting> query = entityManager.createQuery(jpql.toString(), JobPosting.class);

        // 파라미터 바인딩
        if (categoryList != null && !categoryList.isEmpty()) {
            query.setParameter("categoryList", categoryList);
            countQuery.setParameter("categoryList", categoryList);
        }

        if (regionList != null && !regionList.isEmpty()) {
            for (int i = 0; i < regionList.size(); i++) {
                String regionParam = "%" + regionList.get(i) + "%";
                query.setParameter("region" + i, regionParam);
                countQuery.setParameter("region" + i, regionParam);
            }
        }

        if (keyword != null && !keyword.isEmpty()) {
            String keywordParam = "%" + keyword + "%";
            query.setParameter("keyword", keywordParam);
            countQuery.setParameter("keyword", keywordParam);
        }

        if (statusEnum != null) {
            query.setParameter("status", statusEnum);
            countQuery.setParameter("status", statusEnum);
        }

        // 페이징 적용
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // 결과 조회
        List<JobPosting> resultList = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }
}
