# 면접 시작 에러 수정 검증 가이드

면접 시작 시 발생하던 "400 Empty JSON" 에러의 원인을 파악하고, 명확한 에러 메시지를 제공하도록 `GlobalExceptionHandler.java`를 수정했습니다. 이제 서버는 요청 본문이 비었거나 유효하지 않을 때 구체적인 JSON 응답을 반환합니다.

## 변경 사항

- **[GlobalExceptionHandler.java](file:///c:/FuckingNextEnter/NextEnterBack/src/main/java/org/zerock/nextenter/config/GlobalExceptionHandler.java)**:
  - `HttpMessageNotReadableException` 핸들러 추가: 요청 본문(Body) 누락 시 처리.
  - `MethodArgumentNotValidException` 핸들러 추가: 유효성 검사 실패 시 처리.

## 검증 방법

### 1. 백엔드 서버 재시작

수정 사항을 반영하기 위해 실행 중인 **NextEnterBack** 서버를 중지하고 다시 시작해주세요.

### 2. 에러 재현 및 메시지 확인

이전과 동일하게 면접 시작을 시도하여, 이제는 알수 없는 "Empty JSON" 에러 대신 명확한 에러 메시지가 표시되는지 확인합니다.

**예상되는 새 에러 응답 (예시):**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "요청 본문(Body)이 비어있거나 잘못된 형식입니다."
}
```

또는 유효성 검사 실패 시:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "입력값이 유효하지 않습니다.",
  "details": {
    "resumeId": "이력서 ID는 필수입니다",
    "jobCategory": "직무 분류는 필수입니다"
  }
}
```

### 3. 결과 확인

위와 같은 JSON 응답이 보인다면, 프론트엔드나 API 호출 시 원인을 파악할 수 있게 된 것입니다.
검증이 완료되면 알려주세요!
