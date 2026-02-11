# NextEnter Backend

> 구직자와 기업을 AI로 매칭하는 채용 플랫폼 백엔드 시스템

## 📋 프로젝트 개요

NextEnter는 AI 기술을 활용한 채용 플랫폼으로, 이력서 분석, 기업 매칭, 모의 면접 등의 기능을 제공합니다.
Spring Boot 기반의 RESTful API 서버로 구현되었으며, JWT 인증, OAuth2 소셜 로그인, WebSocket 실시간 알림 등을 지원합니다.

## 🏗️ 시스템 아키텍처
```
[React Frontend]
        ↓ (HTTP/HTTPS)
[Spring Boot Backend] ← REST API
        ↓ (JPA)
[MySQL Database]
        ↓
[Python AI Server] ← 이력서 분석/면접 엔진
```

## 🛠️ 기술 스택

### Core
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.6
- **Build Tool**: Gradle 8.x
- **Database**: MySQL 8.x

### Spring Ecosystem
- **Spring Boot Starter Web**: RESTful API 구현
- **Spring Data JPA**: 데이터베이스 ORM
- **Spring Security**: 인증/인가 처리
- **Spring OAuth2 Client**: 소셜 로그인
- **Spring WebSocket**: 실시간 알림
- **Spring Mail**: 이메일 인증
- **Spring AOP**: 횡단 관심사 처리
- **Spring Validation**: 입력 검증

### Security & Authentication
- **JWT (JJWT 0.12.3)**: 토큰 기반 인증
- **BCrypt**: 비밀번호 암호화
- **OAuth2 Client**: 카카오/네이버/구글 소셜 로그인

### Development & Documentation
- **Lombok**: 보일러플레이트 코드 제거
- **Spring Boot DevTools**: 개발 편의성
- **Springdoc OpenAPI 2.6.0**: API 문서 자동화 (Swagger UI)

### File Processing
- **Apache PDFBox 3.0.3**: PDF 이력서 파싱
- **Apache POI 5.2.5**: DOCX 이력서 파싱

### Others
- **Gson 2.10.1**: JSON 처리
- **Jackson**: JSON 직렬화/역직렬화

## 📂 프로젝트 구조
```
OnAndHome/
├── src/main/java/org/zerock/nextenter/
│   ├── config/                          # 설정 클래스
│   │   ├── SecurityConfig.java          # Spring Security 설정
│   │   ├── WebConfig.java               # CORS, Interceptor 설정
│   │   ├── WebSocketConfig.java         # WebSocket 설정
│   │   ├── SwaggerConfig.java           # API 문서 설정
│   │   ├── RestTemplateConfig.java      # HTTP 클라이언트 설정
│   │   └── GlobalExceptionHandler.java  # 전역 예외 처리
│   │
│   ├── user/                            # 사용자 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/User.java
│   │   ├── repository/
│   │   └── DTO/
│   │
│   ├── company/                         # 기업회원 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/Company.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── job/                             # 채용공고 관리
│   │   ├── controller/
│   │   │   ├── JobPostingController.java
│   │   │   └── BookmarkController.java
│   │   ├── service/
│   │   ├── entity/
│   │   │   ├── JobPosting.java
│   │   │   └── Bookmark.java
│   │   ├── repository/
│   │   │   ├── JobPostingRepository.java
│   │   │   ├── JobPostingRepositoryCustom.java
│   │   │   └── JobPostingRepositoryCustomImpl.java
│   │   └── dto/
│   │
│   ├── resume/                          # 이력서 관리
│   │   ├── controller/
│   │   │   ├── ResumeController.java
│   │   │   └── PortfolioRepository.java
│   │   ├── service/
│   │   │   ├── ResumeService.java
│   │   │   ├── FileStorageService.java
│   │   │   ├── ResumeFileTextExtractor.java
│   │   │   ├── ResumeStructureParser.java
│   │   │   ├── PortfolioService.java
│   │   │   ├── StandalonePortfolioService.java
│   │   │   ├── TalentService.java
│   │   │   └── AiResumeClient.java
│   │   ├── entity/
│   │   │   ├── Resume.java
│   │   │   ├── Portfolio.java
│   │   │   ├── SavedTalent.java
│   │   │   └── TalentContact.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── coverletter/                     # 자기소개서 관리
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── CoverLetterService.java
│   │   │   └── CoverLetterFileService.java
│   │   ├── entity/CoverLetter.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── apply/                           # 지원 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/Apply.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── matching/                        # AI 매칭
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/ResumeMatching.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── recommendation/                  # AI 채용공고 추천
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/JobRecommendation.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── interview/                       # AI 모의면접
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── InterviewService.java
│   │   │   └── InterviewAnnotationService.java
│   │   ├── client/
│   │   │   └── AiInterviewClient.java
│   │   ├── entity/
│   │   │   ├── Interview.java
│   │   │   ├── InterviewMessage.java
│   │   │   └── InterviewAnnotation.java
│   │   ├── repository/
│   │   ├── dto/
│   │   └── aop/                         # AOP 관심사
│   │       ├── InterviewAnnotationAspect.java
│   │       ├── InterviewContextAspect.java
│   │       └── InterviewContextHolder.java
│   │
│   ├── interviewoffer/                  # 면접 제안
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/InterviewOffer.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── advertisement/                   # 광고 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/Advertisement.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── credit/                          # 크레딧 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/Credit.java
│   │   ├── repository/
│   │   └── dto/
│   │
│   ├── payment/                         # 결제 처리
│   │   ├── controller/
│   │   └── dto/
│   │
│   ├── notification/                    # 실시간 알림
│   │   ├── Notification.java
│   │   ├── NotificationController.java
│   │   ├── NotificationService.java
│   │   ├── NotificationRepository.java
│   │   ├── NotificationDTO.java
│   │   ├── NotificationSettings.java
│   │   ├── NotificationSettingsController.java
│   │   ├── NotificationSettingsService.java
│   │   ├── NotificationSettingsRepository.java
│   │   └── NotificationSettingsDTO.java
│   │
│   ├── ai/                              # AI 서비스 연동
│   │   └── resume/
│   │       ├── ResumeAiController.java
│   │       ├── ResumeAiService.java
│   │       ├── TestDataController.java
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── repository/
│   │       └── service/
│   │
│   ├── application/                     # 지원 통합 서비스
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── ApplicationIntegrationService.java
│   │   └── dto/
│   │
│   ├── security/                        # 보안 관련
│   │   ├── filter/
│   │   │   └── JWTCheckFilter.java
│   │   ├── handler/
│   │   │   └── OAuth2SuccessHandler.java
│   │   └── service/
│   │       ├── CustomOAuth2User.java
│   │       └── CustomOAuth2UserService.java
│   │
│   ├── service/                         # 공통 서비스
│   │   ├── EmailService.java
│   │   └── VerificationCodeService.java
│   │
│   ├── util/                            # 유틸리티
│   │   ├── JWTUtil.java
│   │   ├── entity/
│   │   │   └── VerificationCode.java
│   │   └── repository/
│   │       └── VerificationCodeRepository.java
│   │
│   ├── common/                          # 공통 상수
│   │   └── constants/
│   │       └── JobConstants.java
│   │
│   └── CodeQueryApplication.java        # 메인 클래스
│
└── src/main/resources/
    ├── application.properties           # 설정 파일
    └── sql/                             # SQL 스크립트
```

## 🔑 핵심 기능

### 1. 인증 및 보안
- **JWT 기반 인증**
  - Access Token: 토큰 기반 API 인증
  - Refresh Token: 자동 토큰 갱신
- **소셜 로그인 (OAuth2)**
  - 카카오, 네이버, 구글 연동
  - 자동 회원가입 및 프로필 동기화
- **이메일 인증**
  - 회원가입 시 이메일 인증
  - 비밀번호 찾기
```java
// JWT 토큰 생성 예시
@Service
public class JWTUtil {
    public String generateToken(Map<String, Object> claims, long expireMin) {
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expireMin * 60 * 1000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }
}
```

### 2. 채용공고 관리
- **CRUD 기능**
  - 공고 등록/수정/삭제 (기업회원)
  - 공고 조회 (전체 사용자)
- **검색 및 필터링**
  - 직무별, 지역별, 경력별 검색
  - 키워드 검색
  - QueryDSL 기반 동적 쿼리
- **북마크 기능**
  - 관심 공고 저장
  - 북마크 목록 관리
```java
// QueryDSL 동적 쿼리 예시
@Override
public Page<JobPosting> searchJobs(String keyword, String location, 
                                    String position, Pageable pageable) {
    BooleanBuilder builder = new BooleanBuilder();
    
    if (keyword != null) {
        builder.and(qJobPosting.title.contains(keyword)
                .or(qJobPosting.description.contains(keyword)));
    }
    
    if (location != null) {
        builder.and(qJobPosting.location.eq(location));
    }
    
    return new PageImpl<>(
        queryFactory.selectFrom(qJobPosting)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch(),
        pageable,
        count
    );
}
```

### 3. 이력서 관리
- **파일 업로드 및 파싱**
  - PDF, DOCX 파일 지원
  - 텍스트 자동 추출
  - 구조화된 데이터 변환
- **이력서 CRUD**
  - 작성/수정/삭제
  - 공개/비공개 설정
- **포트폴리오 관리**
  - 다중 파일 업로드
  - 프로젝트 경력 관리
```java
// 이력서 파일 파싱 예시
@Service
public class ResumeFileTextExtractor {
    public String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    public String extractTextFromDOCX(MultipartFile file) throws IOException {
        XWPFDocument document = new XWPFDocument(file.getInputStream());
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        return extractor.getText();
    }
}
```

### 4. AI 기반 매칭 시스템
- **이력서 분석**
  - Python AI 서버 연동
  - 직무 적합도 분석
  - 강점/약점 분석
- **기업 추천**
  - 이력서 기반 맞춤 기업 추천
  - 매칭 점수 제공
- **채용공고 추천**
  - 사용자 이력서 기반 공고 추천
  - AI 추천 로직
```java
// AI 서버 연동 예시
@Service
public class AiResumeClient {
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${ai.server.url}")
    private String aiServerUrl;
    
    public AiAnalysisResponse analyzeResume(Resume resume) {
        String url = aiServerUrl + "/api/v1/analyze";
        
        ResumeRequest request = ResumeRequest.builder()
            .id(resume.getId().toString())
            .targetRole(resume.getTargetRole())
            .resumeContent(buildResumeContent(resume))
            .build();
        
        return restTemplate.postForObject(url, request, AiAnalysisResponse.class);
    }
}
```

### 5. AI 모의면접
- **대화형 면접**
  - 직무별 맞춤 질문 생성
  - 실시간 피드백
  - 면접 히스토리 저장
- **면접 평가**
  - 답변 분석
  - 종합 점수 산출
  - 개선 사항 제안
- **AOP 기반 어노테이션 처리**
  - 면접 중 특정 키워드 감지
  - 자동 어노테이션 추가
```java
// 면접 서비스 예시
@Service
public class InterviewService {
    @Autowired
    private AiInterviewClient aiClient;
    
    public InterviewQuestionResponse getNextQuestion(
            Long interviewId, String lastAnswer) {
        Interview interview = findById(interviewId);
        
        InterviewRequest request = InterviewRequest.builder()
            .id(interview.getResumeId())
            .targetRole(interview.getTargetRole())
            .lastAnswer(lastAnswer)
            .chatHistory(interview.getMessages())
            .build();
        
        return aiClient.getNextQuestion(request);
    }
}
```

### 6. 실시간 알림 (WebSocket)
- **알림 유형**
  - 지원 상태 변경
  - 면접 제안
  - 북마크 공고 마감 임박
  - 새로운 추천 공고
- **알림 설정**
  - 알림 타입별 on/off
  - 푸시 알림 설정
- **실시간 전송**
  - STOMP over WebSocket
  - 브로커 기반 메시지 전달
```java
// WebSocket 설정 예시
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173")
                .withSockJS();
    }
}
```

### 7. 지원 관리
- **지원하기**
  - 이력서 선택 지원
  - 지원 상태 추적
- **지원 현황**
  - 지원 내역 조회
  - 상태별 필터링 (지원완료, 서류통과, 면접대기, 최종합격, 불합격)
- **기업용 지원자 관리**
  - 지원자 목록 조회
  - 지원서 상세 보기
  - 적합도 분석 (AI 연동)
  - 면접 제안 발송

### 8. 크레딧 및 결제
- **크레딧 시스템**
  - 크레딧 충전
  - 사용 내역 관리
  - AI 기능 이용 시 차감
- **PortOne 결제 연동**
  - 카카오페이, 네이버페이, 토스페이
  - 결제 검증
  - 환불 처리

## 📡 API 엔드포인트

### 인증 API (/api/auth)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | /login | 로그인 | ❌ |
| POST | /register | 회원가입 | ❌ |
| POST | /send-verification | 이메일 인증 코드 발송 | ❌ |
| POST | /logout | 로그아웃 | ✅ |
| GET | /me | 현재 사용자 정보 | ✅ |

### 채용공고 API (/api/jobs)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | / | 공고 목록 조회 | ❌ |
| GET | /{id} | 공고 상세 조회 | ❌ |
| GET | /search | 공고 검색 | ❌ |
| POST | / | 공고 등록 (기업회원) | ✅ |
| PUT | /{id} | 공고 수정 (기업회원) | ✅ |
| DELETE | /{id} | 공고 삭제 (기업회원) | ✅ |

### 이력서 API (/api/resume)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | / | 이력서 목록 | ✅ |
| GET | /{id} | 이력서 상세 | ✅ |
| POST | / | 이력서 등록 | ✅ |
| PUT | /{id} | 이력서 수정 | ✅ |
| DELETE | /{id} | 이력서 삭제 | ✅ |
| POST | /upload | 파일 업로드 | ✅ |

### AI 매칭 API (/api/matching)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | /analyze | 이력서 분석 | ✅ |
| GET | /history | 매칭 히스토리 | ✅ |

### AI 면접 API (/api/interview)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | /start | 면접 시작 | ✅ |
| POST | /next | 다음 질문 | ✅ |
| POST | /complete | 면접 종료 | ✅ |
| GET | /history | 면접 히스토리 | ✅ |
| GET | /result/{id} | 면접 결과 조회 | ✅ |

### 지원 API (/api/apply)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | / | 지원하기 | ✅ |
| GET | / | 지원 내역 | ✅ |
| DELETE | /{id} | 지원 취소 | ✅ |
| PUT | /{id}/status | 상태 변경 (기업회원) | ✅ |

### 북마크 API (/api/bookmarks)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | / | 북마크 목록 | ✅ |
| POST | / | 북마크 추가 | ✅ |
| DELETE | /{id} | 북마크 삭제 | ✅ |

### 알림 API (/api/notifications)
| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | / | 알림 목록 | ✅ |
| PUT | /{id}/read | 읽음 처리 | ✅ |
| DELETE | /{id} | 알림 삭제 | ✅ |
| GET | /settings | 알림 설정 조회 | ✅ |
| PUT | /settings | 알림 설정 변경 | ✅ |

## ⚙️ 환경 변수 설정
```properties
# application.properties

# ============================================
# 데이터베이스 설정
# ============================================
spring.datasource.url=jdbc:mysql://localhost:3306/nextenter?serverTimezone=UTC&characterEncoding=utf8
spring.datasource.username=your_username
spring.datasource.password=your_password

# ============================================
# JPA 설정
# ============================================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# ============================================
# 파일 업로드 설정
# ============================================
file.upload-dir=C:/uploads
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# ============================================
# OAuth2 설정 (카카오)
# ============================================
spring.security.oauth2.client.registration.kakao.client-id=your_kakao_client_id
spring.security.oauth2.client.registration.kakao.client-secret=your_kakao_client_secret
spring.security.oauth2.client.registration.kakao.redirect-uri=http://localhost:8080/login/oauth2/code/kakao

# ============================================
# OAuth2 설정 (네이버)
# ============================================
spring.security.oauth2.client.registration.naver.client-id=your_naver_client_id
spring.security.oauth2.client.registration.naver.client-secret=your_naver_client_secret
spring.security.oauth2.client.registration.naver.redirect-uri=http://localhost:8080/login/oauth2/code/naver

# ============================================
# OAuth2 설정 (구글)
# ============================================
spring.security.oauth2.client.registration.google.client-id=your_google_client_id
spring.security.oauth2.client.registration.google.client-secret=your_google_client_secret
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google

# ============================================
# Gmail SMTP 설정
# ============================================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# ============================================
# AI 서버 설정
# ============================================
ai.server.url=http://localhost:8000/api/v1

# ============================================
# PortOne 결제 설정
# ============================================
portone.api.secret=your_portone_secret_key

# ============================================
# CORS 설정
# ============================================
spring.web.cors.allowed-origins=http://localhost:5173
```

## 🚀 시작하기

### 필수 요구사항
- Java 21 이상
- MySQL 8.0 이상
- Gradle 8.x 이상

### 설치 및 실행

1. **저장소 클론**
```bash
git clone https://github.com/yourusername/NextEnterBack.git
cd NextEnterBack
```

2. **데이터베이스 설정**
```sql
CREATE DATABASE nextenter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **환경 변수 설정**
`src/main/resources/application.properties` 파일을 수정하여 데이터베이스 및 기타 설정을 입력합니다.

4. **의존성 설치 및 빌드**
```bash
./gradlew clean build
```

5. **애플리케이션 실행**
```bash
./gradlew bootRun
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

### API 문서 확인
서버 실행 후 Swagger UI를 통해 API 문서를 확인할 수 있습니다:
- URL: http://localhost:8080/swagger-ui.html

## 🔒 보안 고려사항

### 1. JWT 보안
- 256비트 이상의 강력한 시크릿 키 사용
- Access Token: 짧은 유효기간 권장
- Refresh Token: 안전한 저장소에 보관

### 2. 비밀번호 암호화
- BCrypt 알고리즘 사용
- Salt 자동 생성

### 3. CORS 설정
- 프로덕션 환경에서는 특정 도메인만 허용
- Credentials 사용 시 allowedOriginPatterns 사용

### 4. SQL Injection 방지
- JPA를 통한 파라미터 바인딩
- Native Query 사용 시 파라미터화

### 5. XSS 방지
- Spring Security의 기본 XSS 필터 활용
- 사용자 입력 검증 및 이스케이프

## 🐛 트러블슈팅

### 1. CORS 에러
**증상**: Access-Control-Allow-Origin 에러

**해결**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowCredentials(true);
    // ...
}
```

### 2. JWT 토큰 만료
**증상**: 401 Unauthorized 에러

**해결**: Refresh Token을 이용한 자동 토큰 갱신 구현

### 3. 파일 업로드 실패
**증상**: 파일 크기 제한 에러

**해결**:
```properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### 4. MySQL 연결 실패
**증상**: Connection refused

**해결**:
- MySQL 서버 실행 확인
- 방화벽 설정 확인
- 데이터베이스 권한 확인

## 📝 개발 가이드

### 코드 컨벤션
- **패키지명**: 소문자, 도메인 역순
- **클래스명**: PascalCase
- **메서드명**: camelCase
- **상수**: UPPER_SNAKE_CASE
```java
// 좋은 예
public class UserService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    
    public UserResponse findUserById(Long userId) {
        // ...
    }
}
```

### Git 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드
chore: 빌드 업무, 패키지 설정

예시:
feat: 이력서 파일 업로드 기능 추가
fix: JWT 토큰 갱신 버그 수정
docs: API 문서 업데이트
```

## 📊 데이터베이스 스키마

주요 테이블:
- **users**: 사용자 정보
- **companies**: 기업 정보
- **job_postings**: 채용공고
- **resumes**: 이력서
- **portfolios**: 포트폴리오
- **applies**: 지원 내역
- **bookmarks**: 북마크
- **interviews**: 면접 세션
- **interview_messages**: 면접 대화 내역
- **notifications**: 알림
- **credits**: 크레딧 내역

## 🤝 기여 방법

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 👥 개발자 정보

- **개발자**: 이상연
- **GitHub**: https://github.com/yourusername/NextEnterBack
- **이메일**: dltkddus50@naver.com

## 📞 문의

프로젝트에 대한 질문이나 제안 사항이 있으시면 이슈를 등록해주세요.

---

**NextEnter** - AI가 만드는 새로운 채용 경험
