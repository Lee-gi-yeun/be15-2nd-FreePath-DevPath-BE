package com.freepath.devpath.interview.query.controller;

import com.freepath.devpath.common.dto.ApiResponse;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.interview.query.dto.InterviewRoomDetailResponse;
import com.freepath.devpath.interview.query.dto.InterviewRoomListResponse;
import com.freepath.devpath.interview.query.dto.InterviewSummaryResponse;
import com.freepath.devpath.interview.query.exception.*;
import com.freepath.devpath.interview.query.service.InterviewQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "면접 정보 사용자 조회", description = "사용자의 면접방 목록, 상세정보, 총평 등 조회 기능 API")
@RestController
@RequestMapping("/interview-room")
@RequiredArgsConstructor
public class InterviewQueryController {

    private final InterviewQueryService interviewQueryService;

    /* 사용자가 면접을 진행할 카테고리 목록 조회 */
    @Operation(summary = "면접 카테고리 조회", description = "면접방 생성 시 선택 가능한 카테고리를 반환합니다.")
    @PreAuthorize("hasAnyAuthority('USER')")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getInterviewCategories() {
        List<String> categories = List.of(
                "운영체제", "네트워크", "데이터베이스","자료구조&알고리즘", "디자인 패턴"
                , "객체지향 프로그래밍", "CI/CD", "보안", "클라우드&인프라", "시스템 설계"
        );
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /* 사용자가 진행한 면접방 목록 조회 */
    @Operation(summary = "사용자 면접방 목록 조회", description = "사용자가 진행한 면접방 전체 목록을 조회합니다.")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<InterviewRoomListResponse>> getInterviewRoomList(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Long id = Long.valueOf(userId);
        InterviewRoomListResponse response = interviewQueryService.getInterviewRoomList(id, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* 사용자가 면접방 목록 조회 시 필터 적용 */
    @Operation(summary = "사용자 면접방 필터링 조회", description = "카테고리, 난이도, 평가 기준으로 사용자 면접방을 필터링합니다.")
    @Parameters({
            @Parameter(name = "category", description = "면접 카테고리"),
            @Parameter(name = "difficultyLevel", description = "질문 난이도 (EASY, MEDIUM, HARD)"),
            @Parameter(name = "evaluationStrictness", description = "평가 기준 (LENIENT, NORMAL, STRICT)"),
            @Parameter(name = "page", description = "페이지 번호 (기본값 1)"),
            @Parameter(name = "size", description = "페이지당 크기 (기본값 10)")
    })
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ApiResponse<InterviewRoomListResponse>> getFilteredInterviewRoomList(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String evaluationStrictness,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long id = Long.valueOf(userId);
        InterviewRoomListResponse response = interviewQueryService.getFilteredInterviewRoomList(
                id, category, difficultyLevel, evaluationStrictness, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* 면접방 정보 및 면접 이력 조회 */
    @Operation(summary = "면접방 상세 정보 조회", description = "면접방 ID를 통해 면접방 정보와 질문/답변 이력을 조회합니다.")
    @Parameters({
            @Parameter(name = "interviewRoomId", description = "조회할 면접방 ID", required = true)
    })
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/{interviewRoomId}")
    public ResponseEntity<ApiResponse<InterviewRoomDetailResponse>> getInterviewRoomDetail(
            @PathVariable Long interviewRoomId,
            @AuthenticationPrincipal String userId
    ) {
        Long id = Long.valueOf(userId);
        InterviewRoomDetailResponse response = interviewQueryService.getInterviewRoomByRoomId(interviewRoomId, id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* 면접방의 총평 조회 */
    @Operation(summary = "면접 총평 조회", description = "면접이 종료된 면접방에 대해 GPT가 생성한 총평을 조회합니다.")
    @Parameters({
            @Parameter(name = "interviewRoomId", description = "면접방 ID", required = true)
    })
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/{interviewRoomId}/summary")
    public ResponseEntity<ApiResponse<InterviewSummaryResponse>> getInterviewSummary(
            @PathVariable Long interviewRoomId,
            @AuthenticationPrincipal String userId
    ) {

        Long id = Long.valueOf(userId);
        InterviewSummaryResponse response = interviewQueryService.getInterviewSummary(interviewRoomId, id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // ===== 컨트롤러 레벨 예외 핸들러들 ===== //

    @ExceptionHandler(InterviewRoomQueryCreationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterviewRoomQueryCreationException(InterviewRoomQueryCreationException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(InterviewRoomQueryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterviewRoomQueryNotFoundException(InterviewRoomQueryNotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(InterviewQueryAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterviewQueryAccessException(InterviewQueryAccessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(InterviewQueryHistoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterviewQueryHistoryNotFoundException(InterviewQueryHistoryNotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(InterviewRoomQueryAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterviewRoomQueryAccessException(InterviewRoomQueryAccessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

}