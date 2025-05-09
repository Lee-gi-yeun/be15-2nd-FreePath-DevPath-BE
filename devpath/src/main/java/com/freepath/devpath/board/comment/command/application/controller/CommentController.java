package com.freepath.devpath.board.comment.command.application.controller;

import com.freepath.devpath.board.comment.command.application.dto.CommentResponseDto;
import com.freepath.devpath.board.comment.command.domain.domain.Comment;
import com.freepath.devpath.board.comment.command.application.dto.CommentRequestDto;
import com.freepath.devpath.board.comment.command.application.service.CommentCommandService;
import com.freepath.devpath.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Tag(name = "댓글 및 대댓글", description = "댓글, 대댓글 작성, 수정, 삭제 기능 API")
public class CommentController {

    private final CommentCommandService commentCommandService;

    @PostMapping
    @Operation(summary = "댓글, 대댓글 작성", description = "댓글 또는 대댓글을 작성 합니다.")
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @RequestBody CommentRequestDto dto,
            @AuthenticationPrincipal User userDetails) {

        int userId = Integer.parseInt(userDetails.getUsername());

       CommentResponseDto comment = commentCommandService.saveComment(dto, userId);

        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글, 대댓글 수정", description = "댓글 또는 대댓글을 수정 합니다.")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable int commentId,
            @RequestBody CommentRequestDto dto,
            @AuthenticationPrincipal User userDetails) {

        int userId = Integer.parseInt(userDetails.getUsername());
        commentCommandService.updateComment(commentId, dto.getContents(), userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글, 대댓글 삭제", description = "댓글 또는 대댓글을 삭제 합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable int commentId,
            @AuthenticationPrincipal User userDetails) {

        int userId = Integer.parseInt(userDetails.getUsername());
        commentCommandService.deleteComment(commentId, userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
