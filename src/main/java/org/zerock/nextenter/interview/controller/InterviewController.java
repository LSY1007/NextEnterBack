package org.zerock.nextenter.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.nextenter.interview.dto.*;
import org.zerock.nextenter.interview.service.InterviewService;

import java.util.List;

@Tag(name = "Interview", description = "모의면접 API")
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(summary = "면접 시작", description = "새로운 모의면접 세션을 시작합니다")
    @PostMapping("/start")
    public ResponseEntity<InterviewQuestionResponse> startInterview(
            @RequestParam(required = false) Long userId, // 임시: RequestParam으로 변경
            @Valid @RequestBody InterviewStartRequest request) {

        // 테스트용: userId가 없으면 1로 설정
        if (userId == null) {
            userId = 1L;
        }

        InterviewQuestionResponse response = interviewService.startInterview(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "답변 제출", description = "면접 질문에 대한 답변을 제출하고 다음 질문을 받습니다")
    @PostMapping("/answer")
    public ResponseEntity<InterviewQuestionResponse> submitAnswer(
            @RequestParam(required = false) Long userId, // 임시: RequestParam으로 변경
            @Valid @RequestBody InterviewMessageRequest request) {

        if (userId == null) {
            userId = 1L;
        }

        InterviewQuestionResponse response = interviewService.submitAnswer(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "면접 결과 조회", description = "완료된 면접의 전체 결과를 조회합니다")
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultDTO> getInterviewResult(
            @RequestParam(required = false) Long userId, // 임시: RequestParam으로 변경
            @PathVariable Long interviewId) {

        if (userId == null) {
            userId = 1L;
        }

        InterviewResultDTO result = interviewService.getInterviewResult(userId, interviewId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "면접 히스토리 조회", description = "사용자의 모든 면접 히스토리를 조회합니다")
    @GetMapping("/history")
    public ResponseEntity<List<InterviewHistoryDTO>> getInterviewHistory(
            @RequestParam(required = false) Long userId) { // 임시: RequestParam으로 변경

        if (userId == null) {
            userId = 1L;
        }

        List<InterviewHistoryDTO> history = interviewService.getInterviewHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "면접 취소", description = "진행 중인 면접을 취소합니다")
    @DeleteMapping("/{interviewId}")
    public ResponseEntity<Void> cancelInterview(
            @RequestParam(required = false) Long userId, // 임시: RequestParam으로 변경
            @PathVariable Long interviewId) {

        if (userId == null) {
            userId = 1L;
        }

        interviewService.cancelInterview(userId, interviewId);
        return ResponseEntity.ok().build();
    }
}