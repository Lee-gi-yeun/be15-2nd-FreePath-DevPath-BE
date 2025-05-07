package com.freepath.devpath.interview.query.controller;

import com.freepath.devpath.common.dto.ApiResponse;
import com.freepath.devpath.interview.query.dto.InterviewRoomDto;
import com.freepath.devpath.interview.query.dto.InterviewRoomListResponse;
import com.freepath.devpath.interview.query.service.InterviewQueryAdminService;
import com.freepath.devpath.interview.query.service.InterviewQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "면접 정보 관리자 조회", description = "관리자의 모든 면접방 조회 기능 API")
@RestController
@RequestMapping("/admin/interview-room")
@RequiredArgsConstructor
public class InterviewAdminQueryController {

    private final InterviewQueryAdminService interviewQueryAdminService;

    /* 모든 유저에 대한 면접방 목록 조회 */
    @Operation(summary = "관리자 전체 면접방 목록 조회", description = "모든 유저의 면접방 목록을 페이징으로 조회합니다.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<InterviewRoomListResponse>> getAllInterviewRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        InterviewRoomListResponse response = interviewQueryAdminService.getAllInterviewRooms(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* 모든 유저의 면접방 목록에 필터링 적용 조회 */
    @Operation(summary = "관리자 면접방 필터링 조회", description = "관리자는 상태, 카테고리, 난이도, 평가 기준으로 면접방을 필터링하여 조회할 수 있습니다.")
    @Parameters({
            @Parameter(name = "status", description = "면접방 상태 (PROGRESS, COMPLETED, EXPIRED)"),
            @Parameter(name = "category", description = "면접 카테고리"),
            @Parameter(name = "difficultyLevel", description = "질문 난이도 (EASY, MEDIUM, HARD)"),
            @Parameter(name = "evaluationStrictness", description = "평가 기준 (GENERAL, NORMAL, STRICT)"),
            @Parameter(name = "page", description = "페이지 번호 (기본값 1)"),
            @Parameter(name = "size", description = "페이지당 크기 (기본값 10)")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<InterviewRoomListResponse>> getAdminFilteredInterviewRoomList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String evaluationStrictness,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder, // ✅ 추가됨
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        InterviewRoomListResponse response = interviewQueryAdminService.getAdminFilteredInterviewRoomList(
                status, category, difficultyLevel, evaluationStrictness, sortOrder, page, size); // ✅ 반영됨

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
