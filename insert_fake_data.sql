-- Clean up previous fake data if exists (Optional, be careful in prod)
-- DELETE FROM job_posting WHERE job_id BETWEEN 101 AND 120;
-- DELETE FROM company WHERE company_id BETWEEN 101 AND 120;
-- DELETE FROM resume WHERE resume_id BETWEEN 101 AND 105;

-- ==========================================
-- 0. Insert Test Company Account (firm@gmail.com)
-- ==========================================
INSERT IGNORE INTO company (company_id, email, password, name, phone, business_number, company_name, industry, address, description, employee_count, is_active, created_at, updated_at) VALUES
(999, 'firm@gmail.com', '1234', '테스트기업', '010-1234-5678', '999-99-99999', '테스트 주식회사', 'IT/Service', '서울특별시 테헤란로', '시스템 테스트를 위한 기업 계정입니다.', 10, true, NOW(), NOW());

-- ==========================================
-- 1. Insert Companies (IDs 101-120)
-- ==========================================
INSERT IGNORE INTO company (company_id, email, password, name, phone, business_number, company_name, industry, address, description, employee_count, is_active, created_at, updated_at) VALUES
(101, 'recruit@naver.com', 'pass1234', '네이버채용', '010-1111-2222', '123-45-00101', '네이버 (Naver)', 'Portal/AI', '경기도 성남시 분당구 정자동', '기술로 세상을 연결하는 네이버입니다.', 4000, true, NOW(), NOW()),
(102, 'recruit@kakao.com', 'pass1234', '카카오채용', '010-1111-2222', '123-45-00102', '카카오 (Kakao)', 'Platform/Backend', '제주특별자치도 제주시 / 경기도 성남시', '사람과 세상, 그 이상을 연결하는 카카오입니다.', 3500, true, NOW(), NOW()),
(103, 'recruit@coupang.com', 'pass1234', '쿠팡채용', '010-1111-2222', '123-45-00103', '쿠팡 (Coupang)', 'Commerce/Logistics', '서울특별시 송파구', 'Wow the Customer! 고객 감동을 실현합니다.', 5000, true, NOW(), NOW()),
(104, 'recruit@toss.im', 'pass1234', '토스채용', '010-1111-2222', '123-45-00104', '비바리퍼블리카 (토스)', 'Fintech/Finance', '서울특별시 강남구', '금융의 모든 것을 토스에서 쉽고 간편하게.', 1500, true, NOW(), NOW()),
(105, 'recruit@musinsa.com', 'pass1234', '무신사채용', '010-1111-2222', '123-45-00105', '무신사', 'Fashion Commerce', '서울특별시 성동구', '다 무신사랑 해. 대한민국 1등 패션 플랫폼.', 1200, true, NOW(), NOW()),
(106, 'recruit@dunamu.com', 'pass1234', '두나무채용', '010-1111-2222', '123-45-00106', '두나무 (업비트)', 'Blockchain/Fintech', '서울특별시 강남구', '가장 신뢰받는 글로벌 표준 디지털 자산 거래소.', 500, true, NOW(), NOW()),
(107, 'recruit@socar.com', 'pass1234', '쏘카채용', '010-1111-2222', '123-45-00107', '쏘카', 'Mobility', '서울특별시 성동구', '차가 필요한 모든 순간, 쏘카.', 400, true, NOW(), NOW()),
(108, 'recruit@ably.com', 'pass1234', '에이블리채용', '010-1111-2222', '123-45-00108', '에이블리', 'Commerce/Recommendation', '서울특별시 강남구', '누구나 나만의 스타일을 쉽게 찾을 수 있도록.', 300, true, NOW(), NOW()),
(109, 'recruit@zigzag.com', 'pass1234', '지그재그채용', '010-1111-2222', '123-45-00109', '지그재그', 'Commerce', '서울특별시 강남구', '3500만 여성이 선택한 쇼핑 앱.', 350, true, NOW(), NOW()),
(110, 'recruit@moloco.com', 'pass1234', '몰로코채용', '010-1111-2222', '123-45-00110', '몰로코', 'AdTech/ML', '서울특별시 강남구', '머신러닝으로 비즈니스 성장을 가속화합니다.', 600, true, NOW(), NOW()),
(111, 'recruit@sendbird.com', 'pass1234', '센드버드채용', '010-1111-2222', '123-45-00111', '센드버드', 'Chat Solutions/SaaS', '서울특별시 강남구', '세계 1위 채팅 API 솔루션.', 250, true, NOW(), NOW()),
(112, 'recruit@lunit.com', 'pass1234', '루닛채용', '010-1111-2222', '123-45-00112', '루닛', 'Medical AI', '서울특별시 강남구', 'AI로 암을 정복한다.', 300, true, NOW(), NOW()),
(113, 'recruit@mathpresso.com', 'pass1234', '콴다채용', '010-1111-2222', '123-45-00113', '매스프레소 (콴다)', 'EdTech', '서울특별시 강남구', '가장 효과적인 교육 플랫폼, 콴다.', 400, true, NOW(), NOW()),
(114, 'recruit@ridi.com', 'pass1234', '리디채용', '010-1111-2222', '123-45-00114', '리디', 'Content Platform', '서울특별시 강남구', '마음을 살찌우는 콘텐츠 플랫폼 리디.', 450, true, NOW(), NOW()),
(115, 'recruit@greeting.com', 'pass1234', '두들린채용', '010-1111-2222', '123-45-00115', '두들린 (Greeting)', 'HR SaaS', '서울특별시 강남구', '기업의 채용 문화를 혁신하는 그리팅.', 80, true, NOW(), NOW()),
(116, 'recruit@sparta.com', 'pass1234', '스파르타채용', '010-1111-2222', '123-45-00116', '팀스파르타', 'EdTech', '서울특별시 강남구', '누구나 큰일 낼 수 있는 세상.', 150, true, NOW(), NOW()),
(117, 'recruit@flex.com', 'pass1234', '플렉스채용', '010-1111-2222', '123-45-00117', '플렉스 (flex)', 'HR SaaS', '서울특별시 강남구', '새로운 HR의 시작, flex.', 100, true, NOW(), NOW()),
(118, 'recruit@inflab.com', 'pass1234', '인프랩채용', '010-1111-2222', '123-45-00118', '인프랩 (인프런)', 'Knowledge Sharing', '경기도 성남시', '우리는 성장 기회를 평등하게 만듭니다.', 120, true, NOW(), NOW()),
(119, 'recruit@spoon.com', 'pass1234', '스푼채용', '010-1111-2222', '123-45-00119', '스푼라디오', 'Audio Streaming', '서울특별시 강남구', '오디오로 세상과 소통하다.', 200, true, NOW(), NOW()),
(120, 'recruit@watcha.com', 'pass1234', '왓챠채용', '010-1111-2222', '123-45-00120', '왓챠', 'OTT/Recommendation', '서울특별시 강남구', '발견의 기쁨, 왓챠.', 250, true, NOW(), NOW());

-- ==========================================
-- 2. Insert Job Postings (IDs 101-120) linked to Companies 101-120
-- ==========================================
INSERT IGNORE INTO job_posting (job_id, company_id, title, job_category, required_skills, preferred_skills, experience_min, experience_max, salary_min, salary_max, location, location_city, description, status, view_count, applicant_count, bookmark_count, created_at, updated_at) VALUES
(101, 101, 'AI/ML Engineer (Search & Recommendation)', 'AI', 'Python, PyTorch, TensorFlow', 'Kubernetes, ONNX, Triton Inference Server', 3, 10, 6000, 12000, '경기도 성남시 분당구 정자동', '성남시', 
'<h2>주요 업무</h2><ul><li>네이버 검색 및 추천 시스템을 위한 대규모 AI 모델 학습 및 서빙 최적화</li><li>Milli-second 단위의 실시간 추론 성능 개선 및 경량화</li><li>초거대 언어 모델(LLM) 기반의 서비스 응용 기술 개발</li></ul><h2>자격 요건</h2><ul><li>Python, PyTorch, TensorFlow 활용 능력</li><li>최신 논문 구현 능력</li></ul><h2>우대 사항</h2><ul><li>Kubernetes, ONNX 경험</li><li>대용량 트래픽 처리 경험</li></ul>', 'ACTIVE', 1500, 45, 120, NOW(), NOW()),

(102, 102, 'Backend Developer (Messaging Platform)', 'Backend', 'Kotlin, Spring Boot, MySQL', 'Kafka, Redis, HBase', 5, 15, 7000, 13000, '제주특별자치도 제주시 / 경기도 성남시', '판교/제주', 
'<h2>주요 업무</h2><ul><li>전 국민이 사용하는 메신저 카카오톡 백엔드 시스템 설계 및 개발</li><li>대규모 트래픽 처리를 위한 분산 시스템 아키텍처 고도화</li><li>장애 대응 및 안정적인 서비스 운영을 위한 모니터링 시스템 구축</li></ul><h2>자격 요건</h2><ul><li>Kotlin, Spring Boot 숙련자</li><li>대용량 트래픽 처리 경험</li></ul>', 'ACTIVE', 2000, 60, 200, NOW(), NOW()),

(103, 103, 'Staff Software Engineer (Logistics)', 'Backend', 'Java, Spring Boot, AWS', 'DynamoDB, Kafka, Elasticsearch', 7, 20, 10000, 20000, '서울특별시 송파구', '서울', 
'<h2>주요 업무</h2><ul><li>초고속 배송을 위한 물류 시스템 코어 로직 설계 및 구현</li><li>MSA 기반의 플랫폼 확장성 및 안정성 확보</li></ul><h2>자격 요건</h2><ul><li>Java, Spring Boot 전문가</li><li>MSA 아키텍처 설계 경험</li></ul>', 'ACTIVE', 1800, 20, 150, NOW(), NOW()),

(104, 104, 'Frontend Developer (Core Banking)', 'Frontend', 'TypeScript, React, Next.js', 'React Native, GraphQL, Framer Motion', 3, 10, 6000, 12000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>토스 앱 내 뱅킹 서비스 프론트엔드 개발 및 UX 개선</li><li>복잡한 금융 데이터를 직관적인 UI로 시각화</li></ul><h2>자격 요건</h2><ul><li>TypeScript, React, Next.js 능숙</li><li>UX에 대한 집착</li></ul>', 'ACTIVE', 2200, 80, 250, NOW(), NOW()),

(105, 105, 'Product Owner (Global Store)', 'PM', 'Amplitude, Jira, Figma', 'SQL, Tableau, Python', 5, 15, 7000, 14000, '서울특별시 성동구', '서울', 
'<h2>주요 업무</h2><ul><li>무신사 글로벌 스토어의 제품 로드맵 수립 및 실행</li><li>데이터 기반의 고객 행동 분석을 통한 구매 전환율 개선</li></ul><h2>자격 요건</h2><ul><li>데이터 기반 의사결정 능력</li><li>글로벌 서비스 경험</li></ul>', 'ACTIVE', 1100, 30, 80, NOW(), NOW()),

(106, 106, 'Backend Engineer (Exchange Core)', 'Backend', 'Java, Spring, Redis', 'Netty, Kafka, Blockchain Core', 4, 12, 8000, 16000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>디지털 자산 거래소의 체결 엔진 및 주문 시스템 개발</li><li>금융권 수준의 보안 및 트랜잭션 무결성 보장</li></ul>', 'ACTIVE', 1300, 25, 100, NOW(), NOW()),

(107, 107, 'Data Scientist (Mobility Optimization)', 'AI', 'Python, SQL, Scikit-learn', 'Spark, Airflow, Geopandas', 3, 10, 5000, 10000, '서울특별시 성동구', '서울', 
'<h2>주요 업무</h2><ul><li>차량 배치 및 수요 예측 모델링을 통한 운영 효율화</li><li>이동 데이터를 활용한 유저 행동 분석</li></ul>', 'ACTIVE', 900, 20, 60, NOW(), NOW()),

(108, 108, 'Frontend Engineer (Growth Squad)', 'Frontend', 'React Native, TypeScript, Redux', 'Webview, Amplitude, Braze', 2, 6, 4500, 8000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>에이블리 앱/웹 프론트엔드 기능 개발</li><li>A/B 테스트 환경 구축 및 실험</li></ul>', 'ACTIVE', 1400, 55, 120, NOW(), NOW()),

(109, 109, 'Backend Developer (Order System)', 'Backend', 'Node.js, TypeScript, AWS', 'NestJS, Serverless, DynamoDB', 3, 8, 5000, 9000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>지그재그 주문/결제 시스템 백엔드 API 개발</li><li>입점사 연동 및 정산 시스템 로직 고도화</li></ul>', 'ACTIVE', 1200, 40, 90, NOW(), NOW()),

(110, 110, 'Machine Learning Engineer (Ads Ranking)', 'AI', 'TensorFlow, Go, BigQuery', 'GCP, Kubeflow, C++', 5, 15, 8000, 18000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>모바일 광고 CTR/CVR 예측 모델 개발 및 개선</li><li>실시간 광고 입찰(RTB) 시스템 최적화</li></ul>', 'ACTIVE', 800, 15, 70, NOW(), NOW()),

(111, 111, 'Technical Product Manager (Chat API)', 'PM', 'Rest API, SDK, Technical Writing', 'Python/Java/JS, SaaS Experience', 4, 12, 6000, 12000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>개발자 친화적인 Chat API 및 SDK 제품 기획</li><li>기술 문서 개선 및 DX 향상</li></ul>', 'ACTIVE', 700, 10, 50, NOW(), NOW()),

(112, 112, 'Deep Learning Researcher (Medical Imaging)', 'AI', 'PyTorch, Python, OpenCV', 'Medical Image Analysis, Docker, Git', 2, 8, 5000, 10000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>의료 영상 진단을 위한 Computer Vision 모델 연구 및 개발</li><li>SOTA 논문 리서치 및 독자적인 알고리즘 개선</li></ul>', 'ACTIVE', 1000, 20, 80, NOW(), NOW()),

(113, 113, 'Frontend Developer (Tutoring Platform)', 'Frontend', 'React, TypeScript, Next.js', 'WebRTC, Socket.io, Electron', 3, 9, 5000, 9500, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>콴다 과외 및 튜터링 서비스를 위한 웹 프론트엔드 개발</li><li>실시간 화상 수업 기능을 위한 WebRTC 연동</li></ul>', 'ACTIVE', 1100, 30, 90, NOW(), NOW()),

(114, 114, 'Backend Developer (Content Platform)', 'Backend', 'Go, PHP, MySQL', 'Redis, Kubernetes, Microservices', 4, 10, 5500, 10000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>리디북스 및 콘텐츠 플랫폼의 백엔드 API 서버 개발</li><li>DB 성능 최적화 및 레거시 코드 리팩토링</li></ul>', 'ACTIVE', 950, 25, 75, NOW(), NOW()),

(115, 115, 'Frontend Developer (SaaS)', 'Frontend', 'React, TypeScript, Recoil', 'TanStack Query, Storybook, E2E Testing', 1, 5, 3500, 6000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>채용 관리 솔루션(ATS) 그리팅의 프론트엔드 기능 개발</li><li>복잡한 데이터를 처리하는 어드민/대시보드 UI 구현</li></ul>', 'ACTIVE', 1600, 70, 130, NOW(), NOW()),

(116, 116, 'Backend Developer (LMS)', 'Backend', 'Node.js, Express, MongoDB', 'AWS Lambda, NestJS, Redis', 2, 6, 4000, 7000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>온라인 코딩 교육 플랫폼의 LMS 개발</li><li>서버리스 아키텍처 기반의 기능 확장</li></ul>', 'ACTIVE', 1300, 50, 100, NOW(), NOW()),

(117, 117, 'Frontend Engineer (HR Platform)', 'Frontend', 'Vue.js, Nuxt.js, TypeScript', 'Design System, Jest, Cypress', 3, 8, 5000, 9000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>HR 플랫폼 flex의 웹 클라이언트 개발</li><li>높은 수준의 인터랙션과 디자인 디테일을 갖춘 UI 구현</li></ul>', 'ACTIVE', 1000, 35, 80, NOW(), NOW()),

(118, 118, 'Product Owner (Community)', 'PM', 'Google Analytics, SQL, Notion', 'Community Management, Growth Hacking', 3, 9, 5000, 8500, '경기도 성남시', '판교', 
'<h2>주요 업무</h2><ul><li>인프런 커뮤니티 및 멘토링 서비스의 제품 기획 및 운영</li><li>유저 인게이지먼트 증대를 위한 기능 개선</li></ul>', 'ACTIVE', 900, 25, 60, NOW(), NOW()),

(119, 119, 'Backend Developer (Audio Streaming)', 'Backend', 'Python, Django, AWS', 'WebRTC, Redis, Docker', 2, 7, 4500, 8000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>글로벌 오디오 라이브 스트리밍 서버 개발 및 운영</li><li>실시간 채팅 및 후원 시스템 등 인터랙티브 기능 백엔드 지원</li></ul>', 'ACTIVE', 850, 30, 70, NOW(), NOW()),

(120, 120, 'Backend Developer (Recommendation)', 'Backend', 'Ruby on Rails, Go, AWS', 'Machine Learning, Redis, SQL', 3, 9, 5000, 9000, '서울특별시 강남구', '서울', 
'<h2>주요 업무</h2><ul><li>개인화 추천 시스템 서빙을 위한 백엔드 API 개발</li><li>대용량 별점 데이터 처리 및 분석 파이프라인 운영</li></ul>', 'ACTIVE', 900, 28, 65, NOW(), NOW());

-- ==========================================
-- 3. Insert Resumes (IDs 101-105)
-- ==========================================
INSERT IGNORE INTO resume (resume_id, user_id, resume_name, title, job_category, skills, experiences, status, created_at, updated_at) VALUES
(101, 1, '김신입', '열정 가득한 신입 풀스택 개발자 김신입입니다.', 'Fullstack', 
'["JavaScript", "HTML/CSS", "React", "Node.js", "Express", "MongoDB", "Git"]', 
'[{"title": "팀 프로젝트: 동네 중고거래 마켓 우리동네", "period": "2023.09 ~ 2023.12", "description": "부트캠프 파이널 프로젝트. MERN 스택 활용."}, {"title": "개인 블로그 MyTechLog 개발", "period": "2023.07 ~ 2023.08", "description": "Next.js와 TypeScript 학습용 블로그."}]', 
'COMPLETED', NOW(), NOW()),

(102, 1, '이자바', '기본기가 탄탄한 2년차 풀스택 엔지니어', 'Fullstack', 
'["Java", "Spring Boot", "JPA", "React", "TypeScript", "MySQL", "AWS", "Docker"]', 
'[{"title": "사내 어드민 대시보드 리뉴얼", "period": "2022.03 ~ 현재", "description": "레거시 PHP를 Spring Boot + React로 마이그레이션."}, {"title": "B2B 쇼핑몰 주문 연동 API 개발", "period": "2021.05 ~ 2022.02", "description": "외부 파트너사 연동 RESTful API 개발."}]', 
'COMPLETED', NOW(), NOW()),

(103, 1, '박서버', '비즈니스 가치를 창출하는 5년차 풀스택 개발자', 'Fullstack', 
'["Kotlin", "Spring Boot", "Vue.js", "Redis", "Kafka", "PostgreSQL", "Kubernetes", "MSA"]', 
'[{"title": "글로벌 커머스 플랫폼 MSA 전환", "period": "2021.01 ~ 현재", "description": "Monolithic -> Microservices 전환."}, {"title": "사내 디자인 시스템 구축 리딩", "period": "2019.04 ~ 2020.12", "description": "Vue.js 기반 UI 라이브러리 개발."}]', 
'COMPLETED', NOW(), NOW()),

(104, 1, '최리더', '아키텍처 설계와 팀 리딩이 가능한 8년차 풀스택 개발자', 'Fullstack', 
'["Go", "gRPC", "Next.js", "GraphQL", "Terraform", "AWS", "Elasticsearch", "System Design"]', 
'[{"title": "핀테크 스타트업 초기 멤버 & 테크 리드", "period": "2020.06 ~ 현재", "description": "시리즈 A 투자 유치 및 기술 총괄."}, {"title": "통합 검색 엔진 고도화", "period": "2017.03 ~ 2020.05", "description": "Elasticsearch 기반 상품 검색 엔진 튜닝."}]', 
'COMPLETED', NOW(), NOW()),

(105, 1, '정수석', '기술적 난제를 해결하는 12년차 풀스택 아키텍트', 'Fullstack', 
'["Java", "Spring WebFlux", "React Native", "Rust", "Cloud Native", "DevOps", "AI Engineering"]', 
'[{"title": "전사 클라우드 네이티브 전환 총괄", "period": "2018.02 ~ 현재", "description": "On-premise -> AWS Cloud 전환."}, {"title": "AI 기반 개인화 추천 서비스 아키텍처 설계", "period": "2014.11 ~ 2018.01", "description": "ML 모델 서빙을 위한 하이브리드 앱 아키텍처."}]', 
'COMPLETED', NOW(), NOW());
