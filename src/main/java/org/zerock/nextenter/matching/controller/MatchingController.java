package org.zerock.nextenter.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.zerock.nextenter.matching.dto.MatchingHistoryDTO;
import org.zerock.nextenter.matching.dto.MatchingRequest;
import org.zerock.nextenter.matching.dto.MatchingResultDTO;
import org.zerock.nextenter.matching.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
public class MatchingController {

    private final MatchingService matchingService;

    // 매칭 생성 (AI 매칭 결과 저장)
    @Operation(summary = "매칭 생성")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMatching(@Valid @RequestBody MatchingRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            MatchingResultDTO result = matchingService.createMatching(request);
            response.put("success", true);
            response.put("message", "매칭 결과가 저장되었습니다.");
            response.put("data", result);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("매칭 생성 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ✅ 추가: 사용자별 전체 매칭 히스토리 조회
    @Operation(summary = "사용자별 매칭 히스토리 조회")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getMatchingsByUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<MatchingHistoryDTO> matchings = matchingService.getMatchingsByUserId(userId);
            response.put("success", true);
            response.put("data", matchings);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 매칭 히스토리 조회 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 특정 이력서의 모든 매칭 히스토리 조회
    @Operation(summary = "모든 매칭 히스토리 조회")
    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<Map<String, Object>> getMatchingsByResume(@PathVariable Long resumeId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<MatchingHistoryDTO> matchings = matchingService.getMatchingsByResume(resumeId);
            response.put("success", true);
            response.put("data", matchings);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("매칭 히스토리 조회 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 특정 공고의 모든 매칭 결과 조회
    @Operation(summary = "매칭 결과 조회")
    @GetMapping("/job/{jobId}")
    public ResponseEntity<Map<String, Object>> getMatchingsByJob(@PathVariable Long jobId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<MatchingResultDTO> matchings = matchingService.getMatchingsByJob(jobId);
            response.put("success", true);
            response.put("data", matchings);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("공고별 매칭 조회 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 특정 공고의 특정 등급 매칭 결과 조회
    @Operation(summary = "매칭 등급 조회")
    @GetMapping("/job/{jobId}/grade/{grade}")
    public ResponseEntity<Map<String, Object>> getMatchingsByJobAndGrade(
            @PathVariable Long jobId,
            @PathVariable String grade) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<MatchingResultDTO> matchings = matchingService.getMatchingsByJobAndGrade(jobId, grade);
            response.put("success", true);
            response.put("data", matchings);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("등급별 매칭 조회 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 매칭 상세 조회
    @Operation(summary = "매칭 상세 조회")
    @GetMapping("/{matchingId}")
    public ResponseEntity<Map<String, Object>> getMatchingById(@PathVariable Long matchingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            MatchingResultDTO matching = matchingService.getMatchingById(matchingId);
            response.put("success", true);
            response.put("data", matching);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("매칭 조회 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 매칭 삭제
    @Operation(summary = "매칭 삭제")
    @DeleteMapping("/{matchingId}")
    public ResponseEntity<Map<String, Object>> deleteMatching(@PathVariable Long matchingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            matchingService.deleteMatching(matchingId);
            response.put("success", true);
            response.put("message", "매칭이 삭제되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("매칭 삭제 오류", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}