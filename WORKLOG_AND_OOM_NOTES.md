# 작업 이력 및 OOM/병렬 설정 정리

> 피시방 등 다른 PC에서 이어서 작업할 때 참고용.  
> OOM 가능성 검토 내용, 모델(테스트) 병렬 작업 개수 제한 추천, 현재까지 수정 사항을 정리했다.

---

## 1. OOM 가능성 검토 요약

### 1.1 발생했던 OOM 상황

- **로그**: `hs_err_pid*.log` (NextEnterBack 루트에 다수 존재)
- **메시지**: `There is insufficient memory for the Java Runtime Environment to continue.`
- **원인 요약**:
  - **Native memory allocation 실패**: `mmap` / `malloc` 실패 (G1 virtual space, Chunk::new 등)
  - **물리 메모리**: 시스템 전체 약 16GB, 당시 여유 약 845MB~926MB 수준
  - **Heap**: 테스트 실행 시 `-Xmx512m` 로 동작한 경우 있음 (Gradle Test Executor)
  - **CompressedOops**: Java Heap이 native heap 성장을 막는 구간에 있어, Heap 크기/주소 설정이 영향

### 1.2 JVM이 제안한 대응

- 물리 메모리/스왑 증설
- **Java heap 축소**: `-Xmx` / `-Xms` 감소
- Java 스레드 수·스택 크기(`-Xss`) 감소
- `-XX:HeapBaseMinAddress` 로 Heap 베이스 주소 조정 (32GB/4GB 주소 공간 이슈 시)

### 1.3 현재 build.gradle 에서의 조치

- **테스트 태스크** (`tasks.named('test')`):
  - `maxHeapSize = '1g'`  
    - 512m → 1g 로 올려 테스트 시 OOM 완화 (주석: OOM 방지)
  - `maxParallelForks = 2`  
    - 병렬 테스트 워커 수 제한으로 동시 메모리 사용량 완화

---

## 2. 모델(테스트) 병렬 작업 개수 제한 추천

### 2.1 테스트 (Gradle)

- **현재**: `maxParallelForks = 2`
- **추천**:
  - **일반 PC (RAM 8GB~16GB)**: `2` 유지
  - **피시방/저사양 (RAM 8GB 이하, 다른 프로그램 다수 실행)**: `1` 로 줄이기
  - OOM이 다시 나면: `maxHeapSize` 를 `512m` 로 내리지 말고, `maxParallelForks = 1` 먼저 시도

### 2.2 Spring Boot 실행 시 (애플리케이션)

- IDE/스크립트에서 실행할 때 `-Xmx` 미지정 시 JVM 기본값 사용 → 메모리 넉넉한 환경이면 괜찮지만, 피시방에서는:
  - 예: `-Xmx768m` 또는 `-Xmx1024m` 로 상한 고정 권장
  - 동시에 실행하는 서비스(프론트, NextEnterAI, DB 등) 개수 고려해 전체 RAM 대비 여유 있게 설정

### 2.3 정리

| 환경           | maxParallelForks (테스트) | 테스트 Heap | 비고                    |
|----------------|---------------------------|-------------|-------------------------|
| 권장 (8GB 이상) | 2                         | 1g          | 현재 설정 유지          |
| 저사양/피시방   | 1                         | 1g          | OOM 시 우선 적용 추천   |

---

## 3. 현재까지 수정한 상황 요약

### 3.1 프론트엔드 (NextEnterFront)

- **ResumePage.tsx**
  - 파일 업로드 시 직무를 **고정 "backend"** 가 아니라 **선택값**으로 전송하도록 변경
  - `JOB_CATEGORIES` 사용, 업로드 영역에 직무 선택 드롭다운 추가
  - `uploadJobCategory` state, `jobCategory: uploadJobCategory` 로 전송
- **MatchingPage.tsx**
  - `resume.id` 가 없어서 발생하던 `Cannot read properties of undefined (reading 'toString')` 수정
  - `resumes` 가 undefined 일 수 있음 → `(resumes ?? [])` 처리
  - id 는 `resume.resumeId ?? resume.id` 로 통일, 빈 id 항목 필터
- **InterviewChatPage.tsx**
  - 면접 시작 시 `jobCategory` 를 **선택된 이력서의 직무** 로 보내도록 수정
  - `selectedResume?.industry ?? "미지정"` 사용, fallback 을 "backend" → "미지정" 으로 변경

### 3.2 백엔드 (NextEnterBack)

- **이력서 파일 수신 및 텍스트 추출**
  - `ResumeController`: `createResumeWithFiles` / `updateResumeWithFiles` 에 `@RequestPart("resumeFiles")` 추가
  - `ResumeService`: `createResumeWithFiles`, `updateResumeWithFiles` 시그니처에 `List<MultipartFile> resumeFiles` 추가
  - 첫 번째 `resumeFiles` 에 대해: 파일 저장 → **PDF 텍스트 추출** → `extractedText` 저장
- **의존성**
  - `build.gradle`: `org.apache.pdfbox:pdfbox:3.0.3` 추가
- **신규 클래스**
  - `ResumeFileTextExtractor`: PDF 에서 텍스트 추출
  - `ResumeStructureParser`: `extractedText` 를 규칙 기반으로 파싱해 skills / educations / careers / experiences JSON 생성
- **ResumeService**
  - 이력서 파일 처리 후 `ResumeStructureParser.parse(extractedText)` 호출해 `skills`, `educations`, `careers`, `experiences` 세팅 후 저장
  - 포트폴리오 루프에서 `resumeEntity` 중복 선언 제거 (기존 `resumeEntity` 재사용)

### 3.3 NextEnterAI (Python)

- **main.py**
  - `/interview/next` 요청 시: `resume_content` 에 구조는 비어 있고 `raw_text` 만 있는 경우 `_raw_text_primary` 플래그 설정
  - 구조화 필드(skills/education/professional_experience/project_experience) 비어 있으면 raw_text 를 우선 사용하도록 플래그로 전달
- **interview_engine.py**
  - `_resume_summary_for_prompt()`: 구조가 비어 있으면 `raw_text` 로 프롬프트용 요약 문자열 생성
  - `build_seed_question()`: 위 요약 사용 (raw_text fallback)
  - 직무: `classification.predicted_role` 없을 때 **`target_role`** 사용하도록 수정 (`generate_response` 내부)

---

## 4. 아직 해결 안 된 이슈 (다음 작업 시 참고)

- 사용자 피드백: “해결 안 됐다” → **희망 직무가 여전히 잘못 나오거나**, **raw text 가 skills/experience/education 에 제대로 안 들어가는** 현상이 일부 환경/데이터에서 남아 있을 수 있음
- 확인할 포인트:
  - DB 에 저장된 해당 이력서의 `job_category`, `extracted_text`, `skills`/`educations`/`careers`/`experiences` 실제 값
  - 면접 시작 시 프론트에서 보내는 `jobCategory` 값과 백엔드 → NextEnterAI 로 전달되는 `target_role` 값
  - NextEnterAI 에 들어오는 `resume_content` 안에 `raw_text` / 구조화 필드가 어떻게 채워져 있는지

---

## 5. 파일 위치 참고

| 구분        | 경로 |
|------------|------|
| OOM 로그    | `NextEnterBack/hs_err_pid*.log` |
| 테스트 설정 | `NextEnterBack/build.gradle` (tasks.named('test')) |
| 이력서 파일 처리 | `ResumeController`, `ResumeService`, `ResumeFileTextExtractor`, `ResumeStructureParser` |
| 면접 직무/페이로드 | `NextEnterFront/.../InterviewChatPage.tsx`, `NextEnterAI/app/main.py`, `interview_engine.py` |

이 문서는 `NextEnterBack` 에 두었으며, 피시방에서 이어서 작업할 때 위 내용을 기준으로 OOM 설정과 수정 이력을 확인하면 된다.
