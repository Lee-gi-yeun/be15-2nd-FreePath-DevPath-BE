package com.freepath.devpath.report.controller;

import com.freepath.devpath.common.dto.ApiResponse;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.report.domain.Report;
import com.freepath.devpath.report.dto.request.ReportCheckRequest;
import com.freepath.devpath.report.dto.response.ReportCheckListResponse;
import com.freepath.devpath.report.exception.AlreadyCheckedReportException;
import com.freepath.devpath.report.exception.NoSuchReportCheckException;
import com.freepath.devpath.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
@Tag(name = "신고 관리", description = "게시글 및 댓글 신고, 신고 리스트 확인 및 처리 기능 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/board/{board-id}")
    @Operation(summary = "게시글 신고", description = "특정 게시글에 대해 신고를 등록합니다.")
    public ResponseEntity<ApiResponse<Report>> createBoardReport(
            @Parameter(description = "신고할 게시글 ID", required = true)
            @PathVariable("board-id") int boardId,
            @AuthenticationPrincipal User userDetails
    ) {
        int userId = Integer.parseInt(userDetails.getUsername());
        Report report = reportService.savePostReport(userId, boardId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PostMapping("/comment/{comment-id}")
    @Operation(summary = "댓글 신고", description = "특정 댓글에 대해 신고를 등록합니다.")
    public ResponseEntity<ApiResponse<Report>> createCommentReport(
            @Parameter(description = "신고할 댓글 ID", required = true)
            @PathVariable("comment-id") int commentId,
            @AuthenticationPrincipal User userDetails
    ) {
        int userId = Integer.parseInt(userDetails.getUsername());
        Report report = reportService.saveCommentReport(userId, commentId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/check")
//    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "관리자 신고 검토 리스트 조회", description = "관리자가 처리해야 할 신고 검토 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<ReportCheckListResponse>> getReportCheckList() {
        ReportCheckListResponse response = reportService.getReportCheckList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    @PostMapping("/check/{reportId}")
    // @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "관리자 신고 검토 처리", description = "관리자가 신고 검토 요청을 처리하고 결과를 기록합니다.")
    public ResponseEntity<ApiResponse<Void>> processReportCheck(
            @Parameter(description = "처리할 신고 ID", required = true)
            @PathVariable("reportId") int reportCheckId,
            @Parameter(description = "신고 처리 요청 정보")
            @Validated @ModelAttribute ReportCheckRequest reportCheckRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        int adminId = Integer.parseInt(userDetails.getUsername());
        reportService.processReportCheck(reportCheckId, reportCheckRequest, adminId);
        return ResponseEntity.ok(ApiResponse.success(null));
    } */


    @PostMapping("/check/{reportId}")
    @Operation(summary = "관리자 신고 검토 처리", description = "관리자가 신고 검토 요청을 처리하고 결과를 기록합니다.")
    public ResponseEntity<ApiResponse<Void>> processReportCheck(
            @Parameter(description = "처리할 신고 ID", required = true)
            @PathVariable("reportId") int reportCheckId,
            @Parameter(description = "신고 처리 요청 정보")
            @Validated @ModelAttribute ReportCheckRequest reportCheckRequest
    ) {
        // 인증 관련 부분 임시 주석 처리
        // @AuthenticationPrincipal UserDetails userDetails
        // int adminId = Integer.parseInt(userDetails.getUsername()); // 대신 test용으로 직접 설정

        // 임시 테스트용 사용자 ID 설정
        int adminId = 10; // 테스트용

        try {
            reportService.processReportCheck(reportCheckId, reportCheckRequest, adminId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("500", "서버 오류 발생"));
        }
    }


    @ExceptionHandler(NoSuchReportCheckException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchReportCheckException(NoSuchReportCheckException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(AlreadyCheckedReportException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyCheckedReportException(AlreadyCheckedReportException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }
}
