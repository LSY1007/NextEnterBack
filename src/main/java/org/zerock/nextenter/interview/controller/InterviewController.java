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
    private final org.zerock.nextenter.user.repository.UserRepository userRepository;

    @Operation(summary = "면접 시작", description = "새로운 모의면접 세션을 시작합니다")
    @PostMapping("/start")
    public ResponseEntity<InterviewQuestionResponse> startInterview(
            java.security.Principal principal,
            @Valid @RequestBody InterviewStartRequest request) {

        Long userId = getUserIdFromPrincipal(principal);
        InterviewQuestionResponse response = interviewService.startInterview(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "답변 제출", description = "면접 질문에 대한 답변을 제출하고 다음 질문을 받습니다")
    @PostMapping("/answer")
    public ResponseEntity<InterviewQuestionResponse> submitAnswer(
            java.security.Principal principal,
            @Valid @RequestBody InterviewMessageRequest request) {

        Long userId = getUserIdFromPrincipal(principal);
        // [DEBUG] Log the received answer
        System.out.println("📝 [Controller] submitAnswer called. InterviewId: " + request.getInterviewId() + ", Answer: " + request.getAnswer());
        
        InterviewQuestionResponse response = interviewService.submitAnswer(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "면접 결과 조회", description = "완료된 면접의 전체 결과를 조회합니다")
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultDTO> getInterviewResult(
            java.security.Principal principal,
            @PathVariable Long interviewId) {

        Long userId = getUserIdFromPrincipal(principal);
        InterviewResultDTO result = interviewService.getInterviewResult(userId, interviewId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "면접 히스토리 조회", description = "사용자의 모든 면접 히스토리를 조회합니다")
    @GetMapping("/history")
    public ResponseEntity<List<InterviewHistoryDTO>> getInterviewHistory(
            java.security.Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);
        List<InterviewHistoryDTO> history = interviewService.getInterviewHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "면접 취소", description = "진행 중인 면접을 취소합니다")
    @DeleteMapping("/{interviewId}")
    public ResponseEntity<Void> cancelInterview(
            java.security.Principal principal,
            @PathVariable Long interviewId) {

        Long userId = getUserIdFromPrincipal(principal);
        interviewService.cancelInterview(userId, interviewId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromPrincipal(java.security.Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized user");
        }
        String email = principal.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                .getUserId();
    }
}