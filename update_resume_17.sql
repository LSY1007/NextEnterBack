-- resume_id 17번 풀스택 이력서 업데이트 (이영희)
UPDATE resume SET 
    title = '풀스택 개발자 이력서', 
    job_category = 'Fullstack', 
    resume_name = '이영희', 
    skills = '["Java", "Spring Boot", "JavaScript", "React", "Node.js", "MySQL", "Redis", "Docker", "AWS", "TypeScript"]', 
    experiences = '[{"title": "MSA 기반 이커머스 플랫폼 구축", "period": "2023.03 ~ 2024.01", "description": "Spring Cloud를 활용한 마이크로서비스 아키텍처 설계 및 구현. Redis를 사용한 장바구니 성능 50% 개선. Docker/K8s 기반 배포 자동화 구축."}, {"title": "실시간 협업 대시보드 개발", "period": "2022.06 ~ 2022.12", "description": "React와 Socket.io를 이용한 실시간 데이터 시각화 도구 개발. 복잡한 수치 데이터를 차트화하여 사용자 가독성 향상. 프론트엔드 렌더링 최적화로 화질 저하 없이 응답성 유지."}]', 
    status = 'COMPLETED', 
    updated_at = NOW() 
WHERE resume_id = 17;

-- 업데이트 확인
SELECT resume_id, resume_name, job_category, title, 
       JSON_EXTRACT(experiences, '$[0].title') as first_project,
       JSON_EXTRACT(experiences, '$[1].title') as second_project,
       status, updated_at
FROM resume 
WHERE resume_id = 17;
