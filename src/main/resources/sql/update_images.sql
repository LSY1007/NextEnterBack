-- ============================================
-- 이미지 URL 업데이트 SQL (2026-02-06)
-- ============================================

-- 1. 먼저 현재 company 테이블 확인 (company_id와 company_name 매핑)
SELECT company_id, company_name FROM company ORDER BY company_id;

-- 2. 현재 job_posting 테이블 확인 (job_id와 company_id 매핑)
SELECT job_id, company_id, title FROM job_posting ORDER BY job_id;

-- ============================================
-- 3. COMPANY 테이블 logo_url 업데이트
-- ============================================
-- 주의: company_id는 실제 DB값에 맞게 수정하세요!

-- 네이버
UPDATE company SET logo_url = '/images/companies/logos/네이버 로고.png' WHERE company_name LIKE '%네이버%';
-- 카카오
UPDATE company SET logo_url = '/images/companies/logos/카카오 수정2.png' WHERE company_name LIKE '%카카오%';
-- 쿠팡
UPDATE company SET logo_url = '/images/companies/logos/쿠팡 로고.png' WHERE company_name LIKE '%쿠팡%';
-- 토스
UPDATE company SET logo_url = '/images/companies/logos/토스 로고.jpg' WHERE company_name LIKE '%토스%';
-- 무신사
UPDATE company SET logo_url = '/images/companies/logos/무신사 로고.png' WHERE company_name LIKE '%무신사%';
-- 업비트
UPDATE company SET logo_url = '/images/companies/logos/업비트 수정3.png' WHERE company_name LIKE '%업비트%';
-- 쏘카
UPDATE company SET logo_url = '/images/companies/logos/쏘카 로고.png' WHERE company_name LIKE '%쏘카%';
-- 에이블리
UPDATE company SET logo_url = '/images/companies/logos/에이블리 로고.jpg' WHERE company_name LIKE '%에이블리%';
-- 지그재그
UPDATE company SET logo_url = '/images/companies/logos/지그재그 로고.png' WHERE company_name LIKE '%지그재그%';
-- 몰로코
UPDATE company SET logo_url = '/images/companies/logos/몰로코-로고.png' WHERE company_name LIKE '%몰로코%';
-- 샌드버드/센드버드
UPDATE company SET logo_url = '/images/companies/logos/센드버드 로고.png' WHERE company_name LIKE '%샌드버드%' OR company_name LIKE '%센드버드%';
-- 루닛
UPDATE company SET logo_url = '/images/companies/logos/루닛 로고.png' WHERE company_name LIKE '%루닛%';
-- 매스프레소/콴다
UPDATE company SET logo_url = '/images/companies/logos/메스프레소 로고.png' WHERE company_name LIKE '%매스프레소%' OR company_name LIKE '%콴다%' OR company_name LIKE '%메스프레소%';
-- 리디
UPDATE company SET logo_url = '/images/companies/logos/리디 로고.jpg' WHERE company_name LIKE '%리디%';
-- 두들린
UPDATE company SET logo_url = '/images/companies/logos/두들린_로고-01.png' WHERE company_name LIKE '%두들린%';
-- 팀스파르타
UPDATE company SET logo_url = '/images/companies/logos/팀 스파르타.png' WHERE company_name LIKE '%팀스파르타%' OR company_name LIKE '%팀 스파르타%';
-- 플렉스
UPDATE company SET logo_url = '/images/companies/logos/플렉스.png' WHERE company_name LIKE '%플렉스%' OR company_name LIKE '%플랙스%';
-- 인프랩
UPDATE company SET logo_url = '/images/companies/logos/인프랩 로고.png' WHERE company_name LIKE '%인프랩%';
-- 스푼
UPDATE company SET logo_url = '/images/companies/logos/스푼 로고.png' WHERE company_name LIKE '%스푼%';
-- 왓챠
UPDATE company SET logo_url = '/images/companies/logos/왓챠 로고.png' WHERE company_name LIKE '%왓챠%';


-- ============================================
-- 4. JOB_POSTING 테이블 thumbnail_url, detail_image_url 업데이트
-- ============================================
-- 방법 A: company_id 기반 (company 테이블과 JOIN)

-- 네이버 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/네이버.png',
    jp.detail_image_url = '/images/companies/details/네이버 공채.png'
WHERE c.company_name LIKE '%네이버%';

-- 카카오 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/카카오.png',
    jp.detail_image_url = '/images/companies/details/카카오 공채.png'
WHERE c.company_name LIKE '%카카오%';

-- 쿠팡 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/쿠팡.png',
    jp.detail_image_url = '/images/companies/details/쿠팡 공채.png'
WHERE c.company_name LIKE '%쿠팡%';

-- 토스 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/토스.png',
    jp.detail_image_url = '/images/companies/details/토스 공채.png'
WHERE c.company_name LIKE '%토스%';

-- 무신사 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/무신사.png',
    jp.detail_image_url = '/images/companies/details/무신사 공채.png'
WHERE c.company_name LIKE '%무신사%';

-- 업비트 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/업비트.png',
    jp.detail_image_url = '/images/companies/details/업비트 공채.png'
WHERE c.company_name LIKE '%업비트%';

-- 쏘카 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/쏘카.png',
    jp.detail_image_url = '/images/companies/details/쏘카 공고.png'
WHERE c.company_name LIKE '%쏘카%';

-- 에이블리 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/에이블리.png',
    jp.detail_image_url = '/images/companies/details/에이블리 공고.png'
WHERE c.company_name LIKE '%에이블리%';

-- 지그재그 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/zigzag_thumbnail.png',
    jp.detail_image_url = '/images/companies/details/지그재그 공채.png'
WHERE c.company_name LIKE '%지그재그%';

-- 몰로코 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/몰로코.png',
    jp.detail_image_url = '/images/companies/details/몰로코 공고.png'
WHERE c.company_name LIKE '%몰로코%';

-- 샌드버드/센드버드 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/센드버드.png',
    jp.detail_image_url = '/images/companies/details/샌드버드 공고.png'
WHERE c.company_name LIKE '%샌드버드%' OR c.company_name LIKE '%센드버드%';

-- 루닛 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/루닛.png',
    jp.detail_image_url = '/images/companies/details/루닛 공채.png'
WHERE c.company_name LIKE '%루닛%';

-- 매스프레소/콴다 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/메스프레소.png',
    jp.detail_image_url = '/images/companies/details/매스프레소 (콴다) 공고.png'
WHERE c.company_name LIKE '%매스프레소%' OR c.company_name LIKE '%콴다%' OR c.company_name LIKE '%메스프레소%';

-- 리디 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/리디.png',
    jp.detail_image_url = '/images/companies/details/리디 공고.png'
WHERE c.company_name LIKE '%리디%';

-- 두들린 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/두들린.png',
    jp.detail_image_url = '/images/companies/details/두들린 공채.png'
WHERE c.company_name LIKE '%두들린%';

-- 팀스파르타 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/팀스파르타.png',
    jp.detail_image_url = '/images/companies/details/팀 스파르타 공채.png'
WHERE c.company_name LIKE '%팀스파르타%' OR c.company_name LIKE '%팀 스파르타%';

-- 플렉스 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/플랙스.png',
    jp.detail_image_url = '/images/companies/details/플랙스 공고.png'
WHERE c.company_name LIKE '%플렉스%' OR c.company_name LIKE '%플랙스%';

-- 인프랩 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/인프랩.png',
    jp.detail_image_url = '/images/companies/details/인프랩 공고.png'
WHERE c.company_name LIKE '%인프랩%';

-- 스푼 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/스푼.png',
    jp.detail_image_url = '/images/companies/details/스푼 공고.png'
WHERE c.company_name LIKE '%스푼%';

-- 왓챠 공고
UPDATE job_posting jp
JOIN company c ON jp.company_id = c.company_id
SET jp.thumbnail_url = '/images/companies/thumbnails/왓챠.png',
    jp.detail_image_url = '/images/companies/details/왓챠 공고.png'
WHERE c.company_name LIKE '%왓챠%';


-- ============================================
-- 5. 결과 확인
-- ============================================
SELECT company_id, company_name, logo_url FROM company WHERE logo_url IS NOT NULL;

SELECT job_id, jp.company_id, c.company_name, jp.thumbnail_url, jp.detail_image_url
FROM job_posting jp
JOIN company c ON jp.company_id = c.company_id
WHERE jp.thumbnail_url IS NOT NULL
ORDER BY job_id;
