package com.freepath.devpath.board.post.command.controller;

import com.freepath.devpath.board.post.command.dto.request.PostCreateRequest;
import com.freepath.devpath.board.post.command.dto.request.PostUpdateRequest;
import com.freepath.devpath.board.post.command.exception.FileDeleteFailedException;
import com.freepath.devpath.board.post.command.exception.FileUpdateFailedException;
import com.freepath.devpath.board.post.command.exception.InvalidPostAuthorException;
import com.freepath.devpath.board.post.command.exception.NoSuchPostException;
import com.freepath.devpath.board.post.command.service.PostCommandService;
import com.freepath.devpath.board.post.command.dto.response.PostCreateResponse;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
@Tag(name = "게시글 관리", description = "게시글 업로드, 수정, 삭제 기능 API")
public class PostCommandController {

    private final PostCommandService postCommandService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 업로드", description = "게시글을 작성하고 업로드합니다. 파일은 선택적으로 첨부할 수 있습니다.")
    public ResponseEntity<ApiResponse<PostCreateResponse>> uploadPost(
            @RequestPart @Validated PostCreateRequest postCreateRequest,
            @Parameter(
                    description = "multipart/form-data 형식의 이미지 리스트를 input으로 받습니다. 이때 key 값은 files 입니다.",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "files", required = false) List<MultipartFile> multipartFiles,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostCreateResponse postCreateResponse = postCommandService.createPost(postCreateRequest, multipartFiles, Integer.parseInt(userDetails.getUsername()));

        return ResponseEntity
                .status(HttpStatusCode.CREATED)
                .body(ApiResponse.success(postCreateResponse));
    }

    @PostMapping("/image/temp")
    @Operation(summary = "임시 이미지 업로드", description = "Quill 에디터에서 사용하는 임시 이미지를 S3에 업로드합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadTempImage(
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        int userId = Integer.parseInt(userDetails.getUsername());
        Map<String, String> result = postCommandService.uploadTempImage(image, userId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{board-id}")
    @Operation(summary = "게시글 수정", description = "게시글 ID를 기준으로 게시글을 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable("board-id") int boardId,
            @RequestBody @Validated PostUpdateRequest postUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        int userId = Integer.parseInt(userDetails.getUsername());
        postCommandService.updatePost(postUpdateRequest, boardId, userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{board-id}")
    @Operation(summary = "게시글 삭제", description = "게시글 ID를 기준으로 게시글을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable("board-id") int boardId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        int userId = Integer.parseInt(userDetails.getUsername());
        postCommandService.deletePost(boardId, userId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ===== 컨트롤러 레벨 예외 핸들러들 ===== //

    @ExceptionHandler(NoSuchPostException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchPostException(NoSuchPostException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(InvalidPostAuthorException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPostAuthorException(InvalidPostAuthorException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(FileDeleteFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileDeleteFailedException(FileDeleteFailedException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(FileUpdateFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileUpdateFailedException(FileUpdateFailedException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

}
