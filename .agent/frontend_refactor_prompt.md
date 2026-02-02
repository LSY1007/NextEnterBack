# Frontend Refactoring Prompt

You are tasked with simplifying the **NextEnterFront** application to work with the V2.0 Backend. The goal is to make the frontend a "Dumb Component" that delegates all business logic (score calculation, session management) to the backend.

## Context

The backend is being refactored to remove AOP and centralize logic. The frontend currently contains too much business logic (JSON parsing, score calculation) which is fragile and insecure.

## Objectives

### 1. Simplify `InterviewSetup.tsx`

- **Path**: `src/features/interview/components/InterviewSetup.tsx`
- **Changes**:
  - Remove complex JSON parsing logic for resume/portfolio.
  - The `startInterview` API call should now only send minimal data: `resumeId`, `jobCategory`, `difficulty`.
  - Remove any "mock" or "test" data injection logic that was used for client-side processing.

### 2. Simplify `InterviewChatPage.tsx`

- **Path**: `src/features/interview/components/InterviewChatPage.tsx`
- **Changes**:
  - **Remove Score Calculation**: Delete `handleCompleteInterview`'s complex math logic.
  - **Trust Backend**: Display `finalScore` and `finalFeedback` directly from the backend response.
  - **State Management**: Remove `sessionContext` or any complex state that tracks interview history if the backend manages it. Just display the current question and send the user's answer.

## Key Philosophy

- **"Dumb UI"**: If it involves calculating a score, passing/failing a user, or parsing raw resume data, **delete it**. The backend handles it.
- **Direct Display**: Show exactly what the API returns.

Please refactor the code to meet these objectives.
