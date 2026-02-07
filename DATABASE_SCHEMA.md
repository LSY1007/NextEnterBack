# NextEnter Database Schema (codequery)

> MySQL 8.0 | charset: utf8mb4 | collation: utf8mb4_0900_ai_ci
>
> 접속 정보: `localhost:3306/codequery` / user: `admin` / password: `1111`
>
> 생성일: 2026-02-06

---

## 1. 스키마 생성

```sql
CREATE DATABASE IF NOT EXISTS codequery
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE codequery;
```

---

## 2. 테이블 생성 (총 23개)

### 2-1. user (개인회원)

```sql
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `bio` text,
  `created_at` datetime(6) NOT NULL,
  `detail_address` varchar(255) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `provider` varchar(20) DEFAULT NULL,
  `provider_id` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `withdrawal_code_expiry` datetime(6) DEFAULT NULL,
  `withdrawal_verification_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_email_provider` (`email`,`provider`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-2. users (user 테이블 복제본 - 레거시)

```sql
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `bio` text,
  `created_at` datetime(6) NOT NULL,
  `detail_address` varchar(255) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `provider` varchar(20) DEFAULT NULL,
  `provider_id` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `withdrawal_code_expiry` datetime(6) DEFAULT NULL,
  `withdrawal_verification_code` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_email_provider` (`email`,`provider`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-3. company (기업회원)

```sql
CREATE TABLE `company` (
  `company_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `business_number` varchar(20) NOT NULL,
  `company_name` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `email` varchar(100) NOT NULL,
  `employee_count` int DEFAULT NULL,
  `industry` varchar(50) DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  `ceo_name` varchar(50) DEFAULT NULL,
  `detail_address` varchar(255) DEFAULT NULL,
  `manager_department` varchar(100) DEFAULT NULL,
  `short_intro` varchar(200) DEFAULT NULL,
  `sns_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`company_id`),
  UNIQUE KEY `UKgxn92lo2ky7s5a133c4769qsh` (`business_number`),
  UNIQUE KEY `UKbma9lv19ba3yjwf12a34xord3` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-4. job_posting (채용공고)

```sql
CREATE TABLE `job_posting` (
  `job_id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_count` int NOT NULL,
  `bookmark_count` int NOT NULL,
  `company_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deadline` date DEFAULT NULL,
  `description` text,
  `experience_max` int DEFAULT NULL,
  `experience_min` int DEFAULT NULL,
  `job_category` varchar(50) NOT NULL,
  `location` varchar(100) DEFAULT NULL,
  `preferred_skills` longtext,
  `required_skills` longtext,
  `salary_max` int DEFAULT NULL,
  `salary_min` int DEFAULT NULL,
  `status` enum('ACTIVE','CLOSED','EXPIRED') NOT NULL,
  `title` varchar(200) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `view_count` int NOT NULL,
  `detail_image_url` longtext,
  `location_city` varchar(50) DEFAULT NULL,
  `thumbnail_url` longtext,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-5. resume (이력서)

```sql
CREATE TABLE `resume` (
  `resume_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `extracted_text` longtext,
  `file_path` varchar(255) DEFAULT NULL,
  `file_type` varchar(20) DEFAULT NULL,
  `is_main` bit(1) NOT NULL,
  `job_category` varchar(50) DEFAULT NULL,
  `resume_recommend` longtext,
  `skills` longtext,
  `status` varchar(20) NOT NULL,
  `structured_data` longtext,
  `title` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `view_count` int NOT NULL,
  `visibility` enum('PRIVATE','PUBLIC') NOT NULL,
  `careers` json DEFAULT NULL,
  `certificates` json DEFAULT NULL,
  `educations` json DEFAULT NULL,
  `experiences` json DEFAULT NULL,
  `profile_image` longtext,
  `desired_salary` varchar(50) DEFAULT NULL,
  `resume_address` varchar(200) DEFAULT NULL,
  `resume_birth_date` varchar(20) DEFAULT NULL,
  `resume_detail_address` varchar(100) DEFAULT NULL,
  `resume_email` varchar(100) DEFAULT NULL,
  `resume_gender` varchar(10) DEFAULT NULL,
  `resume_name` varchar(50) DEFAULT NULL,
  `resume_phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`resume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-6. apply (지원)

```sql
CREATE TABLE `apply` (
  `apply_id` bigint NOT NULL AUTO_INCREMENT,
  `ai_score` int DEFAULT NULL,
  `applied_at` datetime(6) NOT NULL,
  `cover_letter_id` bigint DEFAULT NULL,
  `job_id` bigint NOT NULL,
  `notes` text,
  `resume_id` bigint DEFAULT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `status` enum('ACCEPTED','CANCELED','PENDING','REJECTED','REVIEWING') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `document_status` enum('CANCELED','PASSED','PENDING','REJECTED','REVIEWING') NOT NULL,
  `final_status` enum('CANCELED','PASSED','REJECTED') DEFAULT NULL,
  PRIMARY KEY (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-7. cover_letter (자기소개서)

```sql
CREATE TABLE `cover_letter` (
  `cover_letter_id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `created_at` datetime(6) NOT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `file_type` varchar(20) DEFAULT NULL,
  `job_category` varchar(50) DEFAULT NULL,
  `target_company` varchar(100) DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `word_count` int NOT NULL,
  `resume_id` bigint DEFAULT NULL,
  PRIMARY KEY (`cover_letter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-8. bookmark (북마크)

```sql
CREATE TABLE `bookmark` (
  `bookmark_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `job_posting_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`bookmark_id`),
  UNIQUE KEY `uk_user_job` (`user_id`,`job_posting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-9. credit (크레딧)

```sql
CREATE TABLE `credit` (
  `credit_id` bigint NOT NULL AUTO_INCREMENT,
  `balance` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`credit_id`),
  UNIQUE KEY `UK2f7bumcbi2a28ayjjnwus42ho` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-10. advertisements (광고)

```sql
CREATE TABLE `advertisements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `background_color` varchar(100) NOT NULL,
  `button_text` varchar(100) NOT NULL,
  `company_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(500) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `priority` int NOT NULL,
  `target_page` varchar(100) DEFAULT NULL,
  `target_url` varchar(500) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-11. interview (모의면접)

```sql
CREATE TABLE `interview` (
  `interview_id` bigint NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `current_turn` int NOT NULL,
  `difficulty` enum('JUNIOR','SENIOR') NOT NULL,
  `final_feedback` text,
  `final_score` int DEFAULT NULL,
  `job_category` varchar(50) NOT NULL,
  `resume_id` bigint DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','IN_PROGRESS') NOT NULL,
  `total_turns` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`interview_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-12. interview_annotation (면접 분석)

```sql
CREATE TABLE `interview_annotation` (
  `annotation_id` bigint NOT NULL AUTO_INCREMENT,
  `analysis_content` text,
  `created_at` datetime(6) NOT NULL,
  `interview_id` bigint NOT NULL,
  `job_fit_score` double DEFAULT NULL,
  `specificity_score` double DEFAULT NULL,
  `star_compliance_score` double DEFAULT NULL,
  `turn_number` int NOT NULL,
  PRIMARY KEY (`annotation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-13. interview_message (면접 메시지)

```sql
CREATE TABLE `interview_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `interview_id` bigint NOT NULL,
  `message` text NOT NULL,
  `role` enum('CANDIDATE','INTERVIEWER','SYSTEM') NOT NULL,
  `turn_number` int NOT NULL,
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-14. interview_offer (면접 제안)

```sql
CREATE TABLE `interview_offer` (
  `offer_id` bigint NOT NULL AUTO_INCREMENT,
  `apply_id` bigint DEFAULT NULL,
  `company_id` bigint NOT NULL,
  `final_result` enum('PASSED','REJECTED') DEFAULT NULL,
  `interview_status` enum('ACCEPTED','CANCELED','COMPLETED','OFFERED','REJECTED','SCHEDULED') NOT NULL,
  `job_id` bigint NOT NULL,
  `offer_type` enum('COMPANY_INITIATED','FROM_APPLICATION') NOT NULL,
  `offered_at` datetime(6) NOT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  `scheduled_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`offer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-15. job_recommendation (공고 추천)

```sql
CREATE TABLE `job_recommendation` (
  `recommendation_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `credit_used` int NOT NULL,
  `recommended_jobs` longtext NOT NULL,
  `request_data` longtext,
  `resume_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`recommendation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-16. notification_settings (알림 설정)

```sql
CREATE TABLE `notification_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_status_notification` bit(1) NOT NULL,
  `deadline_notification` bit(1) NOT NULL,
  `interview_offer_notification` bit(1) NOT NULL,
  `interview_response_notification` bit(1) NOT NULL,
  `new_application_notification` bit(1) NOT NULL,
  `position_offer_notification` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  `user_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm9ggfvif86mvq5382j88cequn` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-17. notifications (알림)

```sql
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `related_id` bigint DEFAULT NULL,
  `related_type` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `type` enum('APPLICATION_STATUS','DEADLINE_APPROACHING','INTERVIEW_ACCEPTED','INTERVIEW_OFFER','INTERVIEW_REJECTED','INTERVIEW_SCHEDULED','NEW_APPLICATION','POSITION_OFFER') NOT NULL,
  `user_id` bigint NOT NULL,
  `user_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-18. portfolio (포트폴리오)

```sql
CREATE TABLE `portfolio` (
  `portfolio_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `display_order` int DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint NOT NULL,
  `file_type` varchar(20) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `resume_id` bigint NOT NULL,
  PRIMARY KEY (`portfolio_id`),
  KEY `FKri2re2c6us87px87ajlujo0p5` (`resume_id`),
  CONSTRAINT `FKri2re2c6us87px87ajlujo0p5` FOREIGN KEY (`resume_id`) REFERENCES `resume` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-19. resume_ai_recommend (AI 추천 결과)

```sql
CREATE TABLE `resume_ai_recommend` (
  `recommend_id` bigint NOT NULL AUTO_INCREMENT,
  `ai_report` longtext,
  `ai_response` longtext,
  `created_at` datetime(6) NOT NULL,
  `resume_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`recommend_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-20. resume_matching (AI 매칭 기록)

```sql
CREATE TABLE `resume_matching` (
  `matching_id` bigint NOT NULL AUTO_INCREMENT,
  `cons` longtext,
  `created_at` datetime(6) NOT NULL,
  `feedback` text,
  `grade` enum('A','B','C','F','S') NOT NULL,
  `job_id` bigint NOT NULL,
  `matching_type` enum('AI_RECOMMEND','MANUAL') NOT NULL,
  `missing_skills` longtext,
  `pros` longtext,
  `resume_id` bigint NOT NULL,
  `company_name` varchar(200) DEFAULT NULL,
  `score` double DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `resume_grade` enum('A','B','C','F','S') DEFAULT NULL,
  `experience_level` enum('JUNIOR','SENIOR') DEFAULT NULL,
  `job_status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`matching_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-21. saved_talent (저장된 인재)

```sql
CREATE TABLE `saved_talent` (
  `saved_talent_id` bigint NOT NULL AUTO_INCREMENT,
  `company_user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `resume_id` bigint NOT NULL,
  PRIMARY KEY (`saved_talent_id`),
  UNIQUE KEY `UK4p3c9vhmi5o4s7l9n9xa8mxjt` (`company_user_id`,`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-22. talent_contact (인재 연락)

```sql
CREATE TABLE `talent_contact` (
  `contact_id` bigint NOT NULL AUTO_INCREMENT,
  `company_user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `message` text,
  `resume_id` bigint NOT NULL,
  `status` varchar(20) DEFAULT NULL,
  `talent_user_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

### 2-23. verification_code (인증코드)

```sql
CREATE TABLE `verification_code` (
  `verification_id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(100) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `is_used` bit(1) NOT NULL,
  `type` varchar(20) NOT NULL,
  `user_type` varchar(20) NOT NULL,
  PRIMARY KEY (`verification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

---

## 3. 시드 데이터 (필수)

### 3-1. company (20개 기업 + 테스트 계정)

> 비밀번호: 1~20번 기업은 평문 `1234`, 1000번은 bcrypt 해시

```sql
INSERT INTO `company` (`company_id`, `address`, `business_number`, `company_name`, `created_at`, `description`, `email`, `employee_count`, `industry`, `is_active`, `last_login_at`, `logo_url`, `name`, `password`, `phone`, `updated_at`, `website`, `ceo_name`, `detail_address`, `manager_department`, `short_intro`, `sns_url`) VALUES
(1,'경기도 성남시 분당구 정자일로 95','101-01-00001','네이버 (Naver)','2026-02-04 19:53:04.000000','기술로 세상을 연결하는 네이버입니다.','recruit@naver.com',4000,'Portal/AI',1,NULL,'/images/companies/logos/네이버 로고.png','네이버채용','1234','010-0000-0001','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(2,'경기도 성남시 분당구 판교역로 166','101-01-00002','카카오 (Kakao)','2026-02-04 19:53:04.000000','사람과 세상을 연결합니다.','recruit@kakao.com',3000,'Platform/Backend',1,NULL,'/images/companies/logos/카카오 수정2.png','카카오채용','1234','010-0000-0002','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(3,'서울특별시 송파구 송파대로 570','101-01-00003','쿠팡 (Coupang)','2026-02-04 19:53:04.000000','Wow the Customer!','recruit@coupang.com',5000,'Commerce/Logistics',1,NULL,'/images/companies/logos/쿠팡 로고.png','쿠팡채용','1234','010-0000-0003','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(4,'서울특별시 강남구 테헤란로 142','101-01-00004','비바리퍼블리카 (Toss)','2026-02-04 19:53:04.000000','금융의 모든 것, 토스.','recruit@toss.im',1500,'Fintech/Finance',1,NULL,'/images/companies/logos/토스 로고.jpg','토스채용','1234','010-0000-0004','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(5,'서울특별시 성동구 아차산로 13','101-01-00005','무신사 (Musinsa)','2026-02-04 19:53:04.000000','다 무신사랑 해.','recruit@musinsa.com',1200,'Fashion Commerce',1,NULL,'/images/companies/logos/무신사 로고.png','무신사채용','1234','010-0000-0005','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(6,'서울특별시 강남구 테헤란로 4','101-01-00006','두나무 (Upbit)','2026-02-04 19:53:04.000000','가장 신뢰받는 디지털 자산 거래소.','recruit@dunamu.com',500,'Blockchain/Fintech',1,NULL,'/images/companies/logos/업비트 수정3.png','두나무채용','1234','010-0000-0006','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(7,'서울특별시 성동구 왕십리로 83-21','101-01-00007','쏘카 (Socar)','2026-02-04 19:53:04.000000','모빌리티 혁신, 쏘카.','recruit@socar.com',450,'Mobility',1,NULL,'/images/companies/logos/쏘카 로고.png','쏘카채용','1234','010-0000-0007','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(8,'서울특별시 강남구 테헤란로 44길 8','101-01-00008','에이블리 (Ably)','2026-02-04 19:53:04.000000','스타일 커머스 에이블리.','recruit@ably.com',300,'Commerce/Recommendation',1,NULL,'/images/companies/logos/에이블리 로고.jpg','에이블리채용','1234','010-0000-0008','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(9,'서울특별시 강남구 테헤란로 415','101-01-00009','지그재그 (Zigzag)','2026-02-04 19:53:04.000000','3500만 여성의 선택.','recruit@zigzag.com',350,'Commerce',1,NULL,'/images/companies/logos/지그재그 로고.png','지그재그채용','1234','010-0000-0009','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(10,'서울특별시 강남구 테헤란로 521','101-01-00010','몰로코 (Moloco)','2026-02-04 19:53:04.000000','머신러닝으로 비즈니스 성장 가속화.','recruit@moloco.com',600,'AdTech/ML',1,NULL,'/images/companies/logos/몰로코-로고.png','몰로코채용','1234','010-0000-0010','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(11,'서울특별시 강남구 테헤란로 501','101-01-00011','센드버드 (Sendbird)','2026-02-04 19:53:04.000000','글로벌 1위 채팅 API.','recruit@sendbird.com',300,'Chat Solutions/SaaS',1,NULL,'/images/companies/logos/센드버드 로고.png','센드버드채용','1234','010-0000-0011','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(12,'서울특별시 강남구 테헤란로 231','101-01-00012','루닛 (Lunit)','2026-02-04 19:53:04.000000','AI로 암을 정복한다.','recruit@lunit.com',300,'Medical AI',1,NULL,'/images/companies/logos/루닛 로고.png','루닛채용','1234','010-0000-0012','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(13,'서울특별시 강남구 선릉로 428','101-01-00013','매스프레소 (Qanda)','2026-02-04 19:53:04.000000','가장 효과적인 교육 플랫폼.','recruit@mathpresso.com',400,'EdTech',1,NULL,'/images/companies/logos/메스프레소 로고.png','콴다채용','1234','010-0000-0013','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(14,'서울특별시 강남구 테헤란로 325','101-01-00014','리디 (RIDI)','2026-02-04 19:53:04.000000','콘텐츠 플랫폼 리디.','recruit@ridi.com',450,'Content Platform',1,NULL,'/images/companies/logos/리디 로고.jpg','리디채용','1234','010-0000-0014','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(15,'서울특별시 강남구 테헤란로 518','101-01-00015','두들린 (Greeting)','2026-02-04 19:53:04.000000','채용 문화를 혁신하는 그리팅.','recruit@greeting.com',80,'HR SaaS',1,NULL,'/images/companies/logos/두들린_로고-01.png','두들린채용','1234','010-0000-0015','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(16,'서울특별시 강남구 테헤란로44길 8','101-01-00016','팀스파르타 (TeamSparta)','2026-02-04 19:53:04.000000','누구나 큰일 낼 수 있는 세상.','recruit@sparta.com',150,'EdTech',1,NULL,'/images/companies/logos/팀 스파르타.png','스파르타채용','1234','010-0000-0016','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(17,'서울특별시 강남구 테헤란로 503','101-01-00017','플렉스 (flex)','2026-02-04 19:53:04.000000','새로운 HR의 시작.','recruit@flex.com',100,'HR SaaS',1,NULL,'/images/companies/logos/플렉스.png','플렉스채용','1234','010-0000-0017','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(18,'경기도 성남시 분당구 대왕판교로 660','101-01-00018','인프랩 (Inflearn)','2026-02-04 19:53:04.000000','함께 성장하는 인프런.','recruit@inflearn.com',120,'Knowledge Sharing',1,NULL,'/images/companies/logos/인프랩 로고.png','인프랩채용','1234','010-0000-0018','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(19,'서울특별시 강남구 강남대로 419','101-01-00019','스푼라디오 (Spoon)','2026-02-04 19:53:04.000000','오디오로 소통하는 세상.','recruit@spoon.com',200,'Audio Streaming',1,NULL,'/images/companies/logos/스푼 로고.png','스푼채용','1234','010-0000-0019','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(20,'서울특별시 강남구 강남대로 346','101-01-00020','왓챠 (Watcha)','2026-02-04 19:53:04.000000','발견의 기쁨, 왓챠.','recruit@watcha.com',250,'OTT/Recommendation',1,NULL,'/images/companies/logos/왓챠 로고.png','왓챠채용','1234','010-0000-0020','2026-02-04 19:53:04.000000',NULL,NULL,NULL,NULL,NULL,NULL),
(999,'서울특별시 테헤란로','999-99-99999','테스트 주식회사','2026-02-04 16:42:03.000000','시스템 테스트를 위한 기업 계정입니다.','firm@gmail.com',10,'IT/Service',1,NULL,NULL,'테스트기업','1234','010-1234-5678','2026-02-04 16:42:03.000000',NULL,NULL,NULL,NULL,NULL,NULL);
```

### 3-2. job_posting (40개 채용공고)

> 기업당 [신입]+[경력] 2개씩 = 40개. 경력 공고 중 일부는 CLOSED/EXPIRED.

```sql
INSERT INTO `job_posting` (`job_id`, `applicant_count`, `bookmark_count`, `company_id`, `created_at`, `deadline`, `description`, `experience_max`, `experience_min`, `job_category`, `location`, `preferred_skills`, `required_skills`, `salary_max`, `salary_min`, `status`, `title`, `updated_at`, `view_count`, `detail_image_url`, `location_city`, `thumbnail_url`) VALUES
(1,42,120,1,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n검색 플랫폼 대규모 트래픽 MSA 백엔드 개발\nReact·TypeScript 기반 프론트엔드 서비스 개발\nAI·ML 검색 랭킹 및 추천 시스템 연구 개발\n네이버 서비스 기획 PM 및 사용자 경험 UI/UX 설계',2,0,'All','경기도 성남시 분당구 정자일로 95','Kubernetes, AI/ML, AWS, Docker, Figma, Jira, Notion, PyTorch, Git','Java, Spring Boot, Python, Node.js, React, TypeScript, Next.js',7000,5000,'ACTIVE','[신입] NAVER Tech & Service 통합 공채','2026-02-04 19:53:04.000000',1529,'/images/companies/details/네이버 공채.png','분당','/images/companies/thumbnails/네이버.png'),
(2,15,80,1,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n클라우드 인프라 설계 및 대규모 시스템 성능 최적화\nAI·ML 모델 서빙 파이프라인 및 MLOps 구축\n프론트엔드·풀스택 기술 리드 및 아키텍처 설계\n프로덕트 PM 매니지먼트 및 서비스 UX 전략 수립',8,4,'All','경기도 성남시 분당구 정자일로 95','Kubernetes, AI/ML, AWS, Docker, Figma, Jira, Notion, PyTorch, Git','Java, Spring Boot, Python, Node.js, React, TypeScript, Next.js',12000,8000,'ACTIVE','[경력] NAVER Cloud & AI 부문 인재 영입','2026-02-04 19:53:04.000000',2104,'/images/companies/details/네이버 공채.png','분당','/images/companies/thumbnails/네이버.png'),
(3,50,140,2,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n대규모 트래픽 MSA 백엔드 개발\nSpring Boot 클라우드 인프라 성능 최적화\n카카오톡 플랫폼 프론트엔드 및 풀스택 개발\nAI 기반 서비스 기획 PM 및 UI/UX 설계',3,0,'All','경기도 성남시 분당구 판교역로 166','TensorFlow, Docker, Kubernetes, Git, Figma, Jira, Notion','Kotlin, Spring, JPA, Python, Java, Node.js, React',7000,5000,'ACTIVE','[신입] 카카오 신입 크루(Krew) 영입','2026-02-04 19:53:04.000000',1806,'/images/companies/details/카카오 공채.png','판교','/images/companies/thumbnails/카카오.png'),
(4,10,200,2,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n메시징 플랫폼 대규모 분산 시스템 아키텍처 설계\n카카오 서비스 프론트엔드·풀스택 시니어 개발\nAI·ML 모델 연구 및 서비스 적용 고도화\n프로덕트 PM 리드 및 UX 전략 설계',10,5,'All','경기도 성남시 분당구 판교역로 166','TensorFlow, Docker, Kubernetes, Git, Figma, Jira, Notion','Kotlin, Spring, JPA, Python, Java, Node.js, React',15000,10000,'CLOSED','[경력] 카카오 서비스 & 테크 전 직군 영입','2026-02-04 19:53:04.000000',3002,'/images/companies/details/카카오 공채.png','판교','/images/companies/thumbnails/카카오.png'),
(5,60,150,3,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n이커머스 주문·결제·물류 시스템 백엔드 개발\nReact 기반 커머스 프론트엔드 및 풀스택 개발\nAI·ML 상품 추천 및 수요 예측 시스템 개발\n로켓배송 서비스 기획 PM 및 쇼핑 UI/UX 설계',0,0,'All','서울특별시 송파구 송파대로 570','AWS, MSA, Data Engineering, Docker, Kubernetes, Git, Jira','Java, Spring, Python, Node.js, React',8000,6000,'ACTIVE','[신입] 쿠팡 테크 캠퍼스 리쿠르팅','2026-02-04 19:53:04.000000',2200,'/images/companies/details/쿠팡 공채.png','송파','/images/companies/thumbnails/쿠팡.png'),
(6,20,90,3,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n글로벌 이커머스 대규모 트래픽 분산 시스템 설계\n물류·배송 최적화 백엔드 아키텍처 리드\nAI·ML 기반 검색·추천·개인화 엔진 고도화\n프론트엔드 기술 리드 및 프로덕트 PM 전략 수립',10,7,'All','서울특별시 송파구 송파대로 570','AWS, MSA, Data Engineering, Docker, Kubernetes, Git, Jira','Java, Spring, Python, Node.js, React',20000,10000,'ACTIVE','[경력] 쿠팡 글로벌 테크 리더십 채용','2026-02-04 19:53:04.000000',1500,'/images/companies/details/쿠팡 공채.png','송파','/images/companies/thumbnails/쿠팡.png'),
(7,100,300,4,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n금융 서비스 MSA 백엔드 및 결제 시스템 개발\nReact Native 모바일 프론트엔드 및 풀스택 개발\nAI 기반 금융 데이터 분석 및 이상 탐지 개발\n핀테크 서비스 기획 PM 및 금융 UI/UX 설계',2,0,'All','서울특별시 강남구 테헤란로 142','AWS, Docker, Git, Figma, Notion','Kotlin, Spring, Node.js, React, Next.js, Python',9000,6000,'ACTIVE','[신입] 토스 NEXT 개발자 챌린지','2026-02-04 19:53:04.000000',3010,'/images/companies/details/토스 공채.png','강남','/images/companies/thumbnails/토스.png'),
(8,40,250,4,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n대규모 금융 트랜잭션 처리 시스템 아키텍처 설계\n간편결제·뱅킹 서비스 백엔드 성능 최적화\nAI·ML 기반 신용평가 및 리스크 관리 시스템 구축\n프론트엔드 기술 리드 및 핀테크 프로덕트 PM 전략',7,4,'All','서울특별시 강남구 테헤란로 142','AWS, Docker, Git, Figma, Notion','Kotlin, Spring, Node.js, React, Next.js, Python',15000,10000,'CLOSED','[경력] 토스팀 전 계열사 공개 채용','2026-02-04 19:53:04.000000',2500,'/images/companies/details/토스 공채.png','강남','/images/companies/thumbnails/토스.png'),
(9,35,80,5,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n패션 이커머스 플랫폼 백엔드 API 개발\nReact 기반 스타일 커머스 프론트엔드 및 풀스택 개발\nAI 기반 스타일 추천 및 개인화 시스템 개발\n패션 커머스 서비스 기획 PM 및 쇼핑 UI/UX 설계',1,0,'All','서울특별시 성동구 아차산로 13','AWS, Docker, Git, Jira, Notion','Java, Spring, PHP, Vue.js, Python, Node.js, React',7000,5000,'ACTIVE','[신입] 무신사 신입 공채 (Tech/Product)','2026-02-04 19:53:04.000000',1200,'/images/companies/details/무신사 공채.png','성수','/images/companies/thumbnails/무신사.png'),
(10,25,110,5,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n패션 플랫폼 대규모 트래픽 MSA 아키텍처 설계\n커머스 검색·필터링 시스템 백엔드 성능 최적화\nAI·ML 기반 트렌드 분석 및 추천 엔진 고도화\n프론트엔드 기술 리드 및 프로덕트 PM 전략 수립',8,5,'All','서울특별시 성동구 아차산로 13','AWS, Docker, Git, Jira, Notion','Java, Spring, PHP, Vue.js, Python, Node.js, React',13000,9000,'ACTIVE','[경력] 무신사 각 부문 경력 인재 영입','2026-02-04 19:53:04.000000',1800,'/images/companies/details/무신사 공채.png','성수','/images/companies/thumbnails/무신사.png'),
(11,40,130,6,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n블록체인 거래소 실시간 체결 엔진 백엔드 개발\nReact 기반 트레이딩 뷰 프론트엔드 및 풀스택 개발\nAI 기반 이상 거래 탐지 및 시장 분석 시스템 개발\n디지털 자산 서비스 기획 PM 및 트레이딩 UI/UX 설계',3,0,'All','서울특별시 강남구 테헤란로 4','Blockchain, Mobile, AWS, Docker, Kubernetes, Git, Jira','Java, Spring, React, Python',8000,6000,'ACTIVE','[신입] 두나무(업비트) 신입 공채','2026-02-04 19:53:04.000000',1600,'/images/companies/details/업비트 공채.png','강남','/images/companies/thumbnails/업비트.png'),
(12,15,150,6,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n초저지연 거래 매칭 엔진 및 분산 시스템 설계\n블록체인 노드 인프라 및 지갑 시스템 아키텍처\nAI·ML 기반 리스크 관리 및 마켓 인텔리전스 구축\n프론트엔드 기술 리드 및 핀테크 프로덕트 PM 전략',10,4,'All','서울특별시 강남구 테헤란로 4','Blockchain, Mobile, AWS, Docker, Kubernetes, Git, Jira','Java, Spring, React, Python',18000,12000,'CLOSED','[경력] 두나무(업비트) 경력직 채용','2026-02-04 19:53:04.000000',2100,'/images/companies/details/업비트 공채.png','강남','/images/companies/thumbnails/업비트.png'),
(13,20,50,7,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n모빌리티 플랫폼 예약·결제 시스템 백엔드 개발\nReact 기반 카셰어링 서비스 프론트엔드 및 풀스택 개발\nAI 기반 차량 배치 최적화 및 수요 예측 개발\n모빌리티 서비스 기획 PM 및 사용자 경험 UI/UX 설계',0,0,'All','서울특별시 성동구 왕십리로 83-21','Terraform, Data Science, AWS, Docker, Git, Jira','Kotlin, Spring, React, Python, Java',7000,5000,'ACTIVE','[신입] 쏘카 모빌리티 서비스 개발자','2026-02-04 19:53:04.000000',900,'/images/companies/details/쏘카 공고.png','성수','/images/companies/thumbnails/쏘카.png'),
(14,18,70,7,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n모빌리티 플랫폼 MSA 아키텍처 및 인프라 설계\n실시간 차량 관제 시스템 백엔드 성능 최적화\nAI·ML 기반 자율주행 데이터 분석 및 경로 최적화\n프론트엔드 기술 리드 및 모빌리티 프로덕트 PM 전략',8,5,'All','서울특별시 성동구 왕십리로 83-21','Terraform, Data Science, AWS, Docker, Git, Jira','Kotlin, Spring, React, Python, Java',12000,8000,'ACTIVE','[경력] 쏘카 Tech & Biz 리드 영입','2026-02-04 19:53:04.000000',1100,'/images/companies/details/쏘카 공고.png','성수','/images/companies/thumbnails/쏘카.png'),
(15,45,100,8,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n패션 커머스 플랫폼 백엔드 API 및 주문 시스템 개발\nReact 기반 스타일 추천 프론트엔드 및 풀스택 개발\nAI 기반 이미지 검색 및 개인화 추천 시스템 개발\n패션 커머스 서비스 기획 PM 및 쇼핑 UI/UX 설계',3,1,'All','서울특별시 강남구 테헤란로 44길 8','AWS, MLOps, Docker, Git, Figma','Python, Django, React Native, Java, Spring Boot, Node.js, React',7000,5000,'ACTIVE','[신입] 에이블리 성장 메이트(전 직군)','2026-02-04 19:53:04.000000',1300,'/images/companies/details/에이블리 공고.png','강남','/images/companies/thumbnails/에이블리.png'),
(16,10,95,8,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n커머스 플랫폼 대규모 트래픽 처리 아키텍처 설계\n상품 검색·추천 백엔드 시스템 성능 최적화\nAI·ML 기반 스타일 매칭 및 트렌드 분석 고도화\n프론트엔드 기술 리드 및 커머스 프로덕트 PM 전략',9,4,'All','서울특별시 강남구 테헤란로 44길 8','AWS, MLOps, Docker, Git, Figma','Python, Django, React Native, Java, Spring Boot, Node.js, React',12000,8000,'EXPIRED','[경력] 에이블리 핵심 인재 영입','2026-02-04 19:53:04.000000',1400,'/images/companies/details/에이블리 공고.png','강남','/images/companies/thumbnails/에이블리.png'),
(17,30,85,9,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n패션 플랫폼 상품·주문 시스템 백엔드 개발\nReact 기반 스타일 커머스 프론트엔드 및 풀스택 개발\nAI 기반 코디 추천 및 트렌드 분석 시스템 개발\n스타일 커머스 서비스 기획 PM 및 UI/UX 설계',2,0,'All','서울특별시 강남구 테헤란로 415','AWS, GraphQL, Docker, Git, Notion','Node.js, TypeScript, React, Java, Spring Boot, Python',7000,5000,'ACTIVE','[신입] 카카오스타일(지그재그) 신입 영입','2026-02-04 19:53:04.000000',1100,'/images/companies/details/지그재그 공채.png','강남','/images/companies/thumbnails/zigzag_thumbnail.png'),
(18,12,110,9,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n멀티 쇼핑몰 통합 플랫폼 MSA 아키텍처 설계\n커머스 검색·결제 시스템 백엔드 성능 최적화\nAI·ML 개인화 추천 엔진 및 랭킹 시스템 고도화\n프론트엔드 기술 리드 및 패션 프로덕트 PM 전략',10,6,'All','서울특별시 강남구 테헤란로 415','AWS, GraphQL, Docker, Git, Notion','Node.js, TypeScript, React, Java, Spring Boot, Python',14000,9000,'CLOSED','[경력] 카카오스타일(지그재그) 경력 공채','2026-02-04 19:53:04.000000',1500,'/images/companies/details/지그재그 공채.png','강남','/images/companies/thumbnails/zigzag_thumbnail.png'),
(19,15,60,10,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n프로그래매틱 광고 플랫폼 백엔드 시스템 개발\nReact 기반 광고 대시보드 프론트엔드 및 풀스택 개발\nML 기반 광고 타겟팅 및 실시간 입찰 시스템 개발\n애드테크 서비스 기획 PM 및 대시보드 UI/UX 설계',1,0,'All','서울특별시 강남구 테헤란로 521','BigQuery, TensorFlow, GCP, Docker, Git, Kubernetes','Go, Python, Java, Spring Boot, React',9000,7000,'ACTIVE','[신입] 몰로코 글로벌 탤런트','2026-02-04 19:53:04.000000',800,'/images/companies/details/몰로코 공고.png','강남','/images/companies/thumbnails/몰로코.png'),
(20,8,90,10,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n글로벌 애드테크 대규모 실시간 처리 시스템 설계\nDSP·SSP 광고 서빙 백엔드 아키텍처 최적화\nML 기반 CTR 예측 모델 및 추천 엔진 고도화\n프론트엔드 기술 리드 및 글로벌 프로덕트 PM 전략',8,5,'All','서울특별시 강남구 테헤란로 521','BigQuery, TensorFlow, GCP, Docker, Git, Kubernetes','Go, Python, Java, Spring Boot, React',20000,12000,'CLOSED','[경력] 몰로코 각 분야 전문가 채용','2026-02-04 19:53:04.000000',1100,'/images/companies/details/몰로코 공고.png','강남','/images/companies/thumbnails/몰로코.png'),
(21,20,50,11,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n글로벌 채팅 API 플랫폼 백엔드 시스템 개발\nReact 기반 SaaS 대시보드 프론트엔드 및 풀스택 개발\nAI 기반 메시지 모더레이션 및 챗봇 시스템 개발\nB2B SaaS 서비스 기획 PM 및 SDK UI/UX 설계',2,0,'All','서울특별시 강남구 테헤란로 501','AWS, WebSocket, Docker, Git, Jira','Python, Django, Java, Spring Boot, Node.js, React',8000,6000,'ACTIVE','[신입] 센드버드 슈퍼 레이서(Racer) 모집','2026-02-04 19:53:04.000000',700,'/images/companies/details/샌드버드 공고.png','강남','/images/companies/thumbnails/센드버드.png'),
(22,10,70,11,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n글로벌 메시징 인프라 대규모 동시접속 시스템 설계\n실시간 채팅 엔진 백엔드 성능 최적화 및 확장\nAI·ML 기반 스팸 필터링 및 자연어 처리 고도화\n프론트엔드 기술 리드 및 B2B 프로덕트 PM 전략',8,4,'All','서울특별시 강남구 테헤란로 501','AWS, WebSocket, Docker, Git, Jira','Python, Django, Java, Spring Boot, Node.js, React',16000,10000,'ACTIVE','[경력] 센드버드 글로벌 리더 영입','2026-02-04 19:53:04.000000',900,'/images/companies/details/샌드버드 공고.png','강남','/images/companies/thumbnails/센드버드.png'),
(23,25,75,12,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n의료 AI 플랫폼 백엔드 API 및 DICOM 시스템 개발\nReact 기반 의료영상 뷰어 프론트엔드 및 풀스택 개발\nAI·딥러닝 의료영상 분석 및 암 진단 모델 개발\n헬스케어 서비스 기획 PM 및 의료 UI/UX 설계',3,0,'All','서울특별시 강남구 테헤란로 231','PyTorch, Docker, Computer Vision, AWS, Git','Python, Java, Spring Boot, React',7000,5000,'ACTIVE','[신입] 루닛 AI 펠로우십 & 개발자','2026-02-04 19:53:04.000000',950,'/images/companies/details/루닛 공채.png','강남','/images/companies/thumbnails/루닛.png'),
(24,12,100,12,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n의료 AI SaaS 플랫폼 아키텍처 및 인프라 설계\nDICOM 영상 처리 파이프라인 백엔드 성능 최적화\nAI·딥러닝 의료영상 진단 모델 연구 및 FDA 인증\n프론트엔드 기술 리드 및 글로벌 헬스케어 PM 전략',10,5,'All','서울특별시 강남구 테헤란로 231','PyTorch, Docker, Computer Vision, AWS, Git','Python, Java, Spring Boot, React',14000,9000,'ACTIVE','[경력] 루닛 암 정복을 위한 경력직 영입','2026-02-04 19:53:04.000000',1202,'/images/companies/details/루닛 공채.png','강남','/images/companies/thumbnails/루닛.png'),
(25,30,60,13,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n에듀테크 플랫폼 백엔드 API 및 학습 시스템 개발\nReact 기반 교육 서비스 프론트엔드 및 풀스택 개발\nAI 기반 문제 풀이 OCR 및 자동 풀이 엔진 개발\n글로벌 교육 서비스 기획 PM 및 학습 UI/UX 설계',3,1,'All','서울특별시 강남구 선릉로 428','Math OCR, AWS, Docker, Git, Figma','React, TypeScript, Node.js, Java, Spring Boot, Python',7000,5000,'ACTIVE','[신입] 콴다(매스프레소) 글로벌 챌린저','2026-02-04 19:53:04.000000',800,'/images/companies/details/매스프레소 (콴다) 공고.png','강남','/images/companies/thumbnails/메스프레소.png'),
(26,15,90,13,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n글로벌 에듀테크 플랫폼 MSA 아키텍처 설계\n실시간 학습 데이터 처리 백엔드 성능 최적화\nAI·ML 기반 학습 추천 및 자연어 처리 고도화\n프론트엔드 기술 리드 및 글로벌 에듀테크 PM 전략',7,4,'All','서울특별시 강남구 선릉로 428','Math OCR, AWS, Docker, Git, Figma','React, TypeScript, Node.js, Java, Spring Boot, Python',13000,8000,'CLOSED','[경력] 콴다 Tech & Biz 리드 영입','2026-02-04 19:53:04.000000',1300,'/images/companies/details/매스프레소 (콴다) 공고.png','강남','/images/companies/thumbnails/메스프레소.png'),
(27,28,65,14,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n전자책·웹툰 콘텐츠 플랫폼 백엔드 시스템 개발\nReact 기반 리더 뷰어 프론트엔드 및 풀스택 개발\nAI 기반 콘텐츠 추천 및 개인화 시스템 개발\n디지털 콘텐츠 서비스 기획 PM 및 리딩 UI/UX 설계',0,0,'All','서울특별시 강남구 테헤란로 325','Kubernetes, AWS, Docker, Git, Jira','Go, PHP, React, Kotlin, Java, Spring Boot, Python, Node.js',7000,5000,'ACTIVE','[신입] 리디(RIDI) 뉴 크리에이터 모집','2026-02-04 19:53:04.000000',850,'/images/companies/details/리디 공고.png','강남','/images/companies/thumbnails/리디.png'),
(28,10,80,14,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n콘텐츠 플랫폼 대규모 트래픽 분산 시스템 설계\nDRM·스트리밍 백엔드 아키텍처 성능 최적화\nAI·ML 기반 콘텐츠 큐레이션 및 추천 엔진 고도화\n프론트엔드 기술 리드 및 콘텐츠 프로덕트 PM 전략',9,5,'All','서울특별시 강남구 테헤란로 325','Kubernetes, AWS, Docker, Git, Jira','Go, PHP, React, Kotlin, Java, Spring Boot, Python, Node.js',13000,8000,'ACTIVE','[경력] 리디(RIDI) 전 직군 경력 공채','2026-02-04 19:53:04.000000',1152,'/images/companies/details/리디 공고.png','강남','/images/companies/thumbnails/리디.png'),
(29,15,40,15,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\nHR 테크 채용 솔루션 백엔드 API 시스템 개발\nReact 기반 ATS 서비스 프론트엔드 및 풀스택 개발\nAI 기반 이력서 분석 및 인재 매칭 시스템 개발\n채용 솔루션 서비스 기획 PM 및 HR UI/UX 설계',2,0,'All','서울특별시 강남구 테헤란로 518','SaaS, AWS, Docker, Kubernetes, Git, Figma','NestJS, TypeScript, React, Next.js, Java, Spring Boot, Python, Node.js',6500,4500,'ACTIVE','[신입] 두들린(그리팅) 컬처 메이커 모집','2026-02-04 19:53:04.000000',602,'/images/companies/details/두들린 공채.png','강남','/images/companies/thumbnails/두들린.png'),
(30,8,60,15,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\nHR SaaS 플랫폼 MSA 아키텍처 및 인프라 설계\n채용 프로세스 자동화 백엔드 시스템 성능 최적화\nAI·ML 기반 인재 추천 및 적합도 분석 고도화\n프론트엔드 기술 리드 및 HR 프로덕트 PM 전략',8,4,'All','서울특별시 강남구 테헤란로 518','SaaS, AWS, Docker, Kubernetes, Git, Figma','NestJS, TypeScript, React, Next.js, Java, Spring Boot, Python, Node.js',12000,8000,'EXPIRED','[경력] 두들린(그리팅) 스케일업 멤버 영입','2026-02-04 19:53:04.000000',902,'/images/companies/details/두들린 공채.png','강남','/images/companies/thumbnails/두들린.png'),
(31,40,55,16,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n코딩 교육 플랫폼 백엔드 API 및 학습 관리 시스템 개발\nReact 기반 온라인 강의 프론트엔드 및 풀스택 개발\nAI 기반 학습 진도 분석 및 커리큘럼 추천 개발\n에듀테크 서비스 기획 PM 및 학습 플랫폼 UI/UX 설계',1,0,'All','서울특별시 강남구 테헤란로44길 8','MongoDB, AWS Lambda, Docker, Kubernetes, Git','Node.js, Express, React, Java, Spring Boot, Python, TypeScript, Next.js',6500,4500,'ACTIVE','[신입] 팀스파르타 신입 크루 공개 채용','2026-02-04 19:53:04.000000',752,'/images/companies/details/팀 스파르타 공채.png','강남','/images/companies/thumbnails/팀스파르타.png'),
(32,12,75,16,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n온라인 교육 플랫폼 MSA 아키텍처 및 인프라 설계\n실시간 코딩 환경 백엔드 시스템 성능 최적화\nAI·ML 기반 학습 패턴 분석 및 맞춤 교육 고도화\n프론트엔드 기술 리드 및 에듀테크 프로덕트 PM 전략',7,4,'All','서울특별시 강남구 테헤란로44길 8','MongoDB, AWS Lambda, Docker, Kubernetes, Git','Node.js, Express, React, Java, Spring Boot, Python, TypeScript, Next.js',12000,8000,'CLOSED','[경력] 팀스파르타 임팩트 메이커(경력직) 영입','2026-02-04 19:53:04.000000',1050,'/images/companies/details/팀 스파르타 공채.png','강남','/images/companies/thumbnails/팀스파르타.png'),
(33,22,45,17,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\nHR SaaS 급여·근태·인사 관리 백엔드 시스템 개발\nReact 기반 HR 대시보드 프론트엔드 및 풀스택 개발\nAI 기반 인사 데이터 분석 및 자동화 시스템 개발\nHR 솔루션 서비스 기획 PM 및 관리자 UI/UX 설계',2,0,'All','서울특별시 강남구 테헤란로 503','Design System, AWS, Docker, Kubernetes, Git','Vue.js, Nuxt.js, TypeScript, Java, Spring Boot, Python, React, Next.js',7000,5000,'ACTIVE','[신입] 플렉스(flex) 신입 멤버 영입','2026-02-04 19:53:04.000000',700,'/images/companies/details/플랙스 공고.png','강남','/images/companies/thumbnails/플랙스.png'),
(34,10,65,17,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\nHR SaaS 플랫폼 MSA 아키텍처 및 보안 인프라 설계\n급여·세무 정산 엔진 백엔드 시스템 성능 최적화\nAI·ML 기반 인력 분석 및 조직 인사이트 고도화\n프론트엔드 기술 리드 및 HR SaaS 프로덕트 PM 전략',10,6,'All','서울특별시 강남구 테헤란로 503','Design System, AWS, Docker, Kubernetes, Git','Vue.js, Nuxt.js, TypeScript, Java, Spring Boot, Python, React, Next.js',13000,9000,'ACTIVE','[경력] 플렉스(flex) 프로페셔널 경력 채용','2026-02-04 19:53:04.000000',950,'/images/companies/details/플랙스 공고.png','강남','/images/companies/thumbnails/플랙스.png'),
(35,30,60,18,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n온라인 강의 플랫폼 백엔드 API 및 결제 시스템 개발\nReact 기반 학습 플랫폼 프론트엔드 및 풀스택 개발\nAI 기반 강의 추천 및 학습 경로 최적화 시스템 개발\n개발자 교육 서비스 기획 PM 및 학습 UI/UX 설계',3,0,'All','경기도 성남시 분당구 대왕판교로 660','PostgreSQL, Google Analytics, AWS, Docker, K8s, Git','Node.js, React, Next.js, Java, Spring Boot, Python, TypeScript',6500,4500,'ACTIVE','[신입] 인프랩(인프런) 신입 공채','2026-02-04 19:53:04.000000',800,'/images/companies/details/인프랩 공고.png','판교','/images/companies/thumbnails/인프랩.png'),
(36,15,85,18,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n교육 플랫폼 대규모 트래픽 처리 아키텍처 설계\n동영상 스트리밍 백엔드 시스템 성능 최적화\nAI·ML 기반 맞춤 학습 추천 및 콘텐츠 큐레이션 고도화\n프론트엔드 기술 리드 및 교육 프로덕트 PM 전략',9,5,'All','경기도 성남시 분당구 대왕판교로 660','PostgreSQL, Google Analytics, AWS, Docker, K8s, Git','Node.js, React, Next.js, Java, Spring Boot, Python, TypeScript',12000,8000,'CLOSED','[경력] 인프랩(인프런) 가치 창출 멤버 영입','2026-02-04 19:53:04.000000',1100,'/images/companies/details/인프랩 공고.png','판교','/images/companies/thumbnails/인프랩.png'),
(37,18,48,19,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\n오디오 소셜 플랫폼 실시간 스트리밍 백엔드 개발\nReact 기반 라이브 방송 프론트엔드 및 풀스택 개발\nAI 기반 음성 처리 및 콘텐츠 추천 시스템 개발\n오디오 소셜 서비스 기획 PM 및 라이브 UI/UX 설계',3,1,'All','서울특별시 강남구 강남대로 419','WebRTC, Redis, Audio Processing, AWS, Docker, Git','Python, Django, Java, Spring Boot, Node.js, React, TypeScript, Next.js',7000,5000,'ACTIVE','[신입] 스푼라디오 D-J (신입) 리쿠르팅','2026-02-04 19:53:04.000000',650,'/images/companies/details/스푼 공고.png','강남','/images/companies/thumbnails/스푼.png'),
(38,8,62,19,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\n글로벌 오디오 플랫폼 실시간 스트리밍 인프라 설계\nWebRTC·미디어 서버 백엔드 아키텍처 성능 최적화\nAI·ML 기반 음성 인식 및 오디오 콘텐츠 분석 고도화\n프론트엔드 기술 리드 및 글로벌 소셜 프로덕트 PM 전략',8,4,'All','서울특별시 강남구 강남대로 419','WebRTC, Redis, Audio Processing, AWS, Docker, Git','Python, Django, Java, Spring Boot, Node.js, React, TypeScript, Next.js',13000,8000,'ACTIVE','[경력] 스푼라디오 글로벌 파이오니어 영입','2026-02-04 19:53:04.000000',850,'/images/companies/details/스푼 공고.png','강남','/images/companies/thumbnails/스푼.png'),
(39,25,50,20,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 신입 전 직군 채용\nOTT 스트리밍 플랫폼 백엔드 API 및 콘텐츠 시스템 개발\nReact 기반 스트리밍 서비스 프론트엔드 및 풀스택 개발\nAI 기반 콘텐츠 추천 및 사용자 취향 분석 시스템 개발\nOTT 서비스 기획 PM 및 스트리밍 UI/UX 설계',2,0,'All','서울특별시 강남구 강남대로 346','AWS, Recommendation, Docker, Kubernetes, Git','Ruby on Rails, Go, React, Java, Spring Boot, Python, Node.js, TypeScript',6500,4500,'ACTIVE','[신입] 왓챠(Watcha) 디스커버리 팀 모집','2026-02-04 19:53:04.000000',720,'/images/companies/details/왓챠 공고.png','강남','/images/companies/thumbnails/왓챠.png'),
(40,10,72,20,'2026-02-04 19:53:04.000000',NULL,'백엔드 · 프론트엔드 · 풀스택 · PM · AI/ML · UI/UX 경력 전 직군 채용\nOTT 플랫폼 대규모 동영상 스트리밍 인프라 설계\nCDN·트랜스코딩 파이프라인 백엔드 성능 최적화\nAI·ML 기반 추천 알고리즘 및 개인화 엔진 고도화\n프론트엔드 기술 리드 및 엔터테인먼트 프로덕트 PM 전략',10,5,'All','서울특별시 강남구 강남대로 346','AWS, Recommendation, Docker, Kubernetes, Git','Ruby on Rails, Go, React, Java, Spring Boot, Python, Node.js, TypeScript',12000,8000,'CLOSED','[경력] 왓챠(Watcha) 비저너리(Visionary) 채용','2026-02-04 19:53:04.000000',980,'/images/companies/details/왓챠 공고.png','강남','/images/companies/thumbnails/왓챠.png');
```

### 3-3. user (테스트 유저)

```sql
INSERT INTO `user` (`user_id`, `address`, `age`, `bio`, `created_at`, `detail_address`, `email`, `gender`, `is_active`, `last_login_at`, `name`, `password`, `phone`, `profile_image`, `provider`, `provider_id`, `updated_at`, `withdrawal_code_expiry`, `withdrawal_verification_code`) VALUES
(1,NULL,NULL,NULL,'2026-02-04 03:28:55.173178',NULL,'psa828zz@hanmail.net',NULL,1,'2026-02-06 12:03:36.875520','박상원',NULL,NULL,NULL,'NAVER','LaPOypO-h_OAYTeZnbmY1Lrg5-N1lSCcXwUmOGAMubk','2026-02-06 12:03:36.878512',NULL,NULL),
(2,NULL,NULL,NULL,'2026-02-04 12:08:28.378452',NULL,'gweechana@gmail.com','OTHER',1,'2026-02-06 03:20:47.435683','귀차나봇','$2a$10$GHX/flIOwcMMy3EIjHCXbegi5yb.XfU4URPIXsXltruGm7a8/GpUO','01011112222',NULL,NULL,NULL,'2026-02-06 03:20:47.435683',NULL,NULL);
```

### 3-4. credit (크레딧)

```sql
INSERT INTO `credit` (`credit_id`, `balance`, `updated_at`, `user_id`) VALUES
(1,0,'2026-01-22 05:23:26.428899',1),
(2,0,'2026-01-22 05:23:38.881884',3);
```

---

## 4. 공고 상태 요약

| company_id | 기업명 | 신입 (job_id) | 경력 (job_id) |
|:---:|:---|:---:|:---:|
| 1 | 네이버 | ACTIVE (1) | ACTIVE (2) |
| 2 | 카카오 | ACTIVE (3) | **CLOSED** (4) |
| 3 | 쿠팡 | ACTIVE (5) | ACTIVE (6) |
| 4 | 토스 | ACTIVE (7) | **CLOSED** (8) |
| 5 | 무신사 | ACTIVE (9) | ACTIVE (10) |
| 6 | 두나무(업비트) | ACTIVE (11) | **CLOSED** (12) |
| 7 | 쏘카 | ACTIVE (13) | ACTIVE (14) |
| 8 | 에이블리 | ACTIVE (15) | **EXPIRED** (16) |
| 9 | 지그재그 | ACTIVE (17) | **CLOSED** (18) |
| 10 | 몰로코 | ACTIVE (19) | **CLOSED** (20) |
| 11 | 센드버드 | ACTIVE (21) | ACTIVE (22) |
| 12 | 루닛 | ACTIVE (23) | ACTIVE (24) |
| 13 | 콴다 | ACTIVE (25) | **CLOSED** (26) |
| 14 | 리디 | ACTIVE (27) | ACTIVE (28) |
| 15 | 두들린 | ACTIVE (29) | **EXPIRED** (30) |
| 16 | 팀스파르타 | ACTIVE (31) | **CLOSED** (32) |
| 17 | 플렉스 | ACTIVE (33) | ACTIVE (34) |
| 18 | 인프랩 | ACTIVE (35) | **CLOSED** (36) |
| 19 | 스푼라디오 | ACTIVE (37) | ACTIVE (38) |
| 20 | 왓챠 | ACTIVE (39) | **CLOSED** (40) |

---

## 5. 빠른 세팅 순서

```bash
# 1. MySQL에서 codequery 스키마 생성
mysql -u admin -p1111 -e "CREATE DATABASE IF NOT EXISTS codequery DEFAULT CHARACTER SET utf8mb4;"

# 2. 이 파일의 섹션 2 CREATE TABLE문 전부 실행

# 3. 이 파일의 섹션 3 INSERT문 실행 (company → job_posting → user → credit 순)

# 4. Spring Boot 시작 (Hibernate ddl-auto로 누락 컬럼 자동 추가됨)

# 5. (선택) resume, apply 등 트랜잭션 데이터는 앱 사용하며 자동 생성
```

> **참고**: `resume`, `apply`, `interview`, `resume_matching`, `resume_ai_recommend` 등 트랜잭션 테이블은 앱 사용 시 자동 생성되므로 시드 데이터 불필요.
> `users` 테이블은 `user`의 레거시 복제본이므로 비워두어도 무방.
