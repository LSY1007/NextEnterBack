-- 샘플 광고 데이터 추가
-- 기업 ID는 실제 데이터베이스의 company 테이블에 있는 ID로 교체해야 합니다

-- 광고 1: AI 이력서 분석 할인
INSERT INTO advertisements (
    company_id, 
    title, 
    description, 
    background_color, 
    button_text, 
    target_page, 
    is_active, 
    priority, 
    created_at, 
    updated_at
) VALUES (
    1, 
    '🎯 AI 이력서 분석 20% 할인!', 
    '지금 이력서를 분석하고 전문가의 피드백을 받아보세요', 
    'bg-gradient-to-r from-blue-500 to-purple-500', 
    '분석 시작하기', 
    'matching-sub-1', 
    true, 
    10, 
    NOW(), 
    NOW()
);

-- 광고 2: 프리미엄 매칭 서비스
INSERT INTO advertisements (
    company_id, 
    title, 
    description, 
    background_color, 
    button_text, 
    target_page, 
    is_active, 
    priority, 
    created_at, 
    updated_at
) VALUES (
    1, 
    '💼 프리미엄 매칭 서비스', 
    'AI가 추천하는 맞춤 공고로 빠른 취업 성공!', 
    'bg-gradient-to-r from-green-500 to-teal-500', 
    '매칭 받기', 
    'job-sub-2', 
    true, 
    9, 
    NOW(), 
    NOW()
);

-- 광고 3: 모의 면접 체험
INSERT INTO advertisements (
    company_id, 
    title, 
    description, 
    background_color, 
    button_text, 
    target_page, 
    is_active, 
    priority, 
    created_at, 
    updated_at
) VALUES (
    2, 
    '🎤 AI 모의 면접 무료 체험', 
    '실전처럼 연습하고 피드백 받아보세요', 
    'bg-gradient-to-r from-orange-500 to-red-500', 
    '체험하기', 
    'interview-sub-1', 
    true, 
    8, 
    NOW(), 
    NOW()
);

-- 광고 4: 채용 공고 등록 (기업용)
INSERT INTO advertisements (
    company_id, 
    title, 
    description, 
    background_color, 
    button_text, 
    target_url, 
    is_active, 
    priority, 
    created_at, 
    updated_at
) VALUES (
    3, 
    '🏢 우수 인재를 찾고 계신가요?', 
    '채용 공고를 등록하고 맞춤 인재를 찾아보세요', 
    'bg-gradient-to-r from-indigo-500 to-purple-600', 
    '공고 등록하기', 
    'https://nextenter.com/company/register', 
    true, 
    7, 
    NOW(), 
    NOW()
);

-- 광고 조회 (확인용)
SELECT * FROM advertisements ORDER BY priority DESC, created_at DESC;
