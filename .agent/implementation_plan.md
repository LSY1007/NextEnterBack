# 면접 시스템 V2.0 재구축 계획 (The "Slim & Robust" Rewrite)

이 계획은 기존의 복잡하고 깨지기 쉬운 면접 시스템을 완전히 걷어내고, **"프론트엔드는 단순하게, 백엔드는 튼튼하게"** 재구축하는 것을 목표로 합니다.

## User Review Required

> [!WARNING]
> 이 변경은 기존의 `InterviewService` 핵심 로직과 Frontend `InterviewChatPage`의 구조를 대대적으로 변경합니다. **기존 코드는 호환되지 않을 수 있습니다.**

## Proposed Changes

### 1. Backend Refactoring [NextEnterBack]

핵심: **AOP 제거** 및 **책임의 중앙화 (Research Papers 철학 계승)**

#### [MODIFY] [InterviewStartRequest.java](file:///c:/FuckingNextEnter/NextEnterBack/src/main/java/org/zerock/nextenter/interview/dto/InterviewStartRequest.java)

- 불필요한 `resumeContent`, `portfolio` 등의 복잡한 중첩 객체 필드 제거.
- **단순화**: `resumeId`, `jobCategory`, `difficulty` 정도만 받음.
- 나머지 데이터는 백엔드가 DB에서 직접 조회하여 **AI 프롬프트의 Context**로 구성.

#### [MODIFY] [InterviewService.java](file:///c:/FuckingNextEnter/NextEnterBack/src/main/java/org/zerock/nextenter/interview/service/InterviewService.java)

- **AOP 제거 & Explicit Logic**: `InterviewContextHolder`를 걷어내고, 서비스 레이어에서 명시적으로 데이터를 흐르게 하여 에러 추적이 가능하게 함.
- **Dialogic Feedback 강화**: AI 응답을 처리할 때, 단순 질문 이동이 아닌 '대화형 피드백(Conversate 논문 지향)'이 가능하도록 LLM 요청 구조를 개선.
- **점수 계산 이동**: Frontend의 점수 계산 로직을 `submitAnswer` 내부로 이동. 마지막 질문 답변 시 자동으로 점수 계산 및 저장 수행.

#### [DELETE] AOP Components

- `InterviewContextAspect.java`
- `InterviewContextHolder.java`
- `InterviewAnnotationAspect.java` (관련된 어노테이션 AOP 모두 제거)

### 2. Frontend Simplification [NextEnterFront]

핵심: **로직 제거 (Dumb Component)**

#### [MODIFY] [InterviewSetup.tsx](file:///c:/FuckingNextEnter/NextEnterFront/src/features/interview/components/InterviewSetup.tsx)

- **역할 축소**: 사용자 입력(포트폴리오 텍스트 등)만 받고, 복잡한 JSON 파싱 로직 제거.
- `startInterview` 호출 시 최소한의 ID와 텍스트 데이터만 전송.

#### [MODIFY] [InterviewChatPage.tsx](file:///c:/FuckingNextEnter/NextEnterFront/src/features/interview/components/InterviewChatPage.tsx)

- **점수 계산 로직 전면 삭제**: `handleCompleteInterview` 내의 복잡한 수학 계산 로직 삭제. 백엔드가 주는 `finalScore`, `finalFeedback`을 그대로 표시하도록 변경.
- **상태 관리 단순화**: `sessionContext` 등 불필요한 상태 제거.

## Verification Plan

### Manual Verification Flow

1. **면접 시작**: `면접 시작` 버튼 클릭 -> 백엔드 200 OK 및 첫 질문 수신 확인. (400 에러 사라져야 함)
2. **대화 진행**: 답변 입력 -> 백엔드 전송 -> AI 질문 수신 확인.
3. **면접 종료**: 마지막 답변 전송 -> 백엔드에서 계산된 "합격/불합격" 및 "점수"가 즉시 표시되는지 확인.
