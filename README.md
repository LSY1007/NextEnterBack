# 🚀 TheCareer 프로젝트 팀 협업 가이드

이 문서는 우리 프로젝트의 코드 관리 규칙을 담고 있습니다. 모든 팀원은 원활한 협업을 위해 아래 규칙을 반드시 숙지해 주세요.

## 🌿 브랜치 전략 (Git Flow)

우리 프로젝트는 3단계 브랜치 체계를 사용합니다.

- **main**: 최종 배포 브랜치. (머지 마스터 전용 완성 전까지 push X)
- **develop**: 모든 기능이 합쳐지는 개발 중심 브랜치.
- **feat/이름-기능명**: 각자 맡은 기능을 작업하는 개인 브랜치.

## 🛠 작업 순서 (매일 지켜주세요!)

### 1. 작업 시작 전: develop의 최신 내용을 가져옵니다.

```bash
git checkout develop
git pull origin develop
```

### 2. 브랜치 생성: 자신의 기능을 만들 브랜치를 만듭니다.

```bash
git checkout -b feat/이름-기능명
```

### 3. 코드 작업 및 커밋: 커밋 규칙에 맞춰 기록합니다.

### 4. 서버에 올리기: 작업이 끝나면 내 브랜치를 서버에 올립니다.

```bash
git push origin feat/이름-기능명
```

### 5. 머지 요청: 기능 완성시에만 Pull Request 요청 받습니다.

## 📝 커밋 메시지 규칙

커밋 메시지는 아래와 같은 형식을 지켜주세요.

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정 (README 등)
- `style`: 코드 포맷팅, 세미콜론 누락 등 (코드 변경 없음)
- `chore`: 빌드 업무 수정, 패키지 매니저 설정 등

**예시**: `feat: 로그인 페이지 이메일 유효성 검사 추가`

## 🚨 충돌(Conflict) 발생 시

- 충돌이 발생하면 당황하지 말고 즉시 머지 마스터에게 알리세요.
- 절대로 혼자 force push를 하지 않습니다.

## 📅 주차별 기록표 (Progress Tracker)

## ⚙️ Git 설정 파일 설명

### `.gitignore`

Git에서 추적하지 않을 파일과 디렉토리를 지정합니다.

**OS & IDE 파일**

- `.DS_Store`, `Thumbs.db`: 운영체제에서 자동 생성되는 파일
- `.idea/`, `.vscode/`: IDE 설정 디렉토리
- `*.iws`, `*.iml`, `*.ipr`: IntelliJ IDEA 설정 파일

**Spring/Java 빌드 산출물**

- `build/`, `out/`, `.gradle/`, `bin/`: 빌드 결과물 및 캐시 디렉토리
- `src/main/resources/application-local.properties`: 로컬 환경 설정 파일 (민감 정보 포함 가능)

### `.gitattributes`

Git이 파일을 처리하는 방식을 정의합니다.

- **텍스트 파일 자동 감지**: 모든 파일을 텍스트로 자동 인식하고 줄바꿈 문자를 정규화합니다.
- **파일 타입별 설정**: Java, Properties, XML, YAML 등 각 파일 타입에 맞는 처리를 지정합니다.
- **줄바꿈 문자 통일**:
  - Shell 스크립트(`*.sh`): LF (Linux/Mac)
  - Batch 파일(`*.bat`): CRLF (Windows)

이 설정으로 운영체제 간 줄바꿈 문자 차이로 인한 문제를 방지하고, 코드 일관성을 유지합니다.
