# SQLite 백엔드 연동 — 에이전트 명령 가이드

> **상황**: 피시방 등에서 MySQL 포트(3306) 연결이 차단되어 사용 불가. 프로젝트 폴더 내 SQLite로 전환해 백엔드를 실행하려는 경우, 다음 에이전트가 SQLite에 맞게 설정·스키마를 적용할 수 있도록 안내합니다.

---

## 1. 배경 (에이전트에게 전달할 맥락)

- **현재 백엔드**: Spring Boot + JPA, MySQL (`localhost:3306/codequery`) 사용
- **설정 파일**: `NextEnterBack/src/main/resources/application.properties`
- **엔티티 위치**: `NextEnterBack/src/main/java/org/zerock/nextenter/` 하위 각 도메인별 `entity/` 패키지
- **목표**: MySQL 대신 **프로젝트 폴더 내 SQLite 파일**(예: `./local.db`) 사용, 동일 엔티티 구조로 테이블 생성 및 애플리케이션 기동

---

## 2. 에이전트에게 붙여넣을 명령어 (1단계)

채팅창에 **아래 문장을 그대로 복사해 먼저 입력**하세요.

```
나는 지금 피시방이라서 MySQL 대신 프로젝트 폴더 내의 SQLite를 사용하려고 해. 내가 아래에 주는 엔티티(Entity) 클래스들을 읽고, 이 구조와 똑같은 SQLite용 'CREATE TABLE' 쿼리를 만들어줘. 그리고 application.properties(또는 yml) 설정도 SQLite에 맞게 수정해줘.
```

- 프로젝트 루트가 `FuckingNextEnter`이고, 백엔드가 `NextEnterBack`이라면, **“프로젝트 폴더” = NextEnterBack 또는 FuckingNextEnter** 라고 한 줄 추가해도 됩니다.  
  예: *“프로젝트 폴더는 NextEnterBack 이고, DB 파일은 NextEnterBack/local.db 로 쓸게.”*

---

## 3. 엔티티 코드 제공하기 (2단계)

위 명령어를 보낸 뒤, **엔티티 파일(.java) 내용을 모두 복사해 붙여넣기** 하세요.

- **파일이 여러 개**여도 한 번에 모두 붙여넣으면 에이전트가 함께 분석합니다.
- 이 프로젝트의 엔티티 파일 위치(참고용):

| 도메인 | 엔티티 파일 경로 (NextEnterBack 기준) |
|--------|--------------------------------------|
| User | `src/main/java/org/zerock/nextenter/user/entity/User.java` |
| Resume | `src/main/java/org/zerock/nextenter/resume/entity/Resume.java` |
| Interview | `src/main/java/org/zerock/nextenter/interview/entity/Interview.java`, `InterviewMessage.java`, `InterviewAnnotation.java` |
| Apply | `src/main/java/org/zerock/nextenter/apply/entity/Apply.java` |
| CoverLetter | `src/main/java/org/zerock/nextenter/coverletter/entity/CoverLetter.java` |
| Job | `src/main/java/org/zerock/nextenter/job/entity/JobPosting.java`, `Bookmark.java` |
| Company | `src/main/java/org/zerock/nextenter/company/entity/Company.java` |
| Advertisement | `src/main/java/org/zerock/nextenter/advertisement/entity/Advertisement.java` |
| InterviewOffer | `src/main/java/org/zerock/nextenter/interviewoffer/entity/InterviewOffer.java` |
| Notification | `src/main/java/org/zerock/nextenter/notification/Notification.java`, `NotificationSettings.java` |
| Credit | `src/main/java/org/zerock/nextenter/credit/entity/Credit.java` |
| Resume 관련 | `TalentContact.java`, `SavedTalent.java`, `Portfolio.java` (resume/entity) |
| AI/추천 | `ResumeAiRecommend.java`, `JobRecommendation.java`, `ResumeMatching.java` |
| 기타 | `VerificationCode.java` (util/entity) |

---

## 4. 에이전트가 해줄 작업 정리 (체크리스트)

다음 에이전트가 아래를 수행하면 SQLite 백엔드 연동이 됩니다.

1. **SQLite용 CREATE TABLE**
   - 제공한 엔티티와 동일한 컬럼·타입·제약조건을 SQLite 문법으로 변환
   - `AUTO_INCREMENT` → `AUTOINCREMENT`, 타입 매핑(예: `BIGINT` → `INTEGER`) 등

2. **application.properties 수정**
   - `spring.datasource.url=jdbc:sqlite:./local.db` (또는 `NextEnterBack/local.db` 등 프로젝트 내 경로)
   - `spring.datasource.driver-class-name=org.sqlite.JDBC`
   - `spring.jpa.database-platform` → SQLite용 Dialect (예: `org.hibernate.community.dialect.SQLiteDialect`)
   - MySQL 전용 옵션(username/password 등) 제거 또는 주석

3. **build.gradle 의존성**
   - `runtimeOnly 'org.xerial:sqlite-jdbc:3.x.x'` 추가
   - (Hibernate 6.2+ community dialect 사용 시) `org.hibernate.orm:hibernate-community-dialects` 추가 여부 안내

4. **ddl-auto**
   - 개발용: `update` 또는 `create` 유지/안내  
   - 수동 스키마만 쓸 경우: `none` + 에이전트가 준 CREATE TABLE 스크립트 사용 안내

---

## 5. DB 툴(안티그라비티 등)에서 적용하기 (3단계)

에이전트가 **SQLite용 CREATE TABLE**을 주면:

1. 우측 하단 또는 사이드바의 **[Database]** 아이콘 클릭
2. **[Add Connection]** → **[SQLite]** 선택
3. 파일 경로: `./local.db` (또는 `NextEnterBack/local.db`) — 프로젝트 폴더 안에 생성됨
4. 에이전트가 준 **SQL 전체**를 복사해 **[SQL Editor]** 에 붙여넣고 **Run** 실행

이후 Spring Boot 애플리케이션을 기동하면 SQLite에 연결됩니다.

---

## 6. 초보자용 팁

| 질문 | 답변 |
|------|------|
| 에러가 나면? | 에러 메시지 전체를 복사해 에이전트에게 "이 에러 해결해줘"라고 보내면 원인 분석 및 수정 방법을 제안해 줍니다. |
| 데이터는 어디에? | 프로젝트 폴더 안에 생긴 `local.db` (또는 지정한 경로) 한 파일입니다. 이 파일만 구글 드라이브·USB 등에 복사해 두면 데이터를 옮길 수 있습니다. |
| MySQL로 다시 돌리려면? | `application.properties`에서 SQLite 설정을 주석 처리하고, 기존 MySQL url/username/password/driver 설정을 다시 활성화하면 됩니다. |

---

## 7. 이 문서 위치

- **파일**: `NextEnterBack/SQLITE_AGENT_GUIDE.md`
- **용도**: 피시방 등 MySQL 사용 불가 환경에서, 다음 에이전트가 SQLite로 백엔드를 연동할 때 사용할 명령어·절차·체크리스트 제공.

이 가이드를 에이전트에게 먼저 붙여넣거나, “SQLITE_AGENT_GUIDE.md 보고 SQLite 연동해줘”라고 지시하면 동일한 절차로 진행할 수 있습니다.
