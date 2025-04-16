package com.freepath.devpath.chatting.command.application.controller;

import com.freepath.devpath.chatting.command.application.dto.request.GroupChattingRoomCreateRequest;
import com.freepath.devpath.chatting.command.application.dto.request.GroupChattingRoomUpdateRequest;
import com.freepath.devpath.chatting.command.application.dto.response.ChattingRoomCommandResponse;
import com.freepath.devpath.chatting.command.application.service.ChattingRoomCommandService;
import com.freepath.devpath.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "채팅방 관리", description = "채팅방 생성/수정/삭제 기능 API")
public class ChattingRoomCommandController {
    private final ChattingRoomCommandService chattingRoomCommandService;

    @Operation(summary = "일대일 채팅방 생성", description = "일대일 채팅방을 생성한다.")
    @PostMapping("/chatting/create/{userId}")
    public ResponseEntity<ApiResponse<ChattingRoomCommandResponse>> createChattingRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int userId
    ){
        ChattingRoomCommandResponse chattingRoomCommandResponse =
                chattingRoomCommandService.createChattingRoom(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.success(chattingRoomCommandResponse));
    }

    @Operation(summary = "그룹 채팅방 생성", description = "게시글 ID를 이용하여 그룹 채팅방을 생성한다.")
    @PostMapping("/chatting/create/group")
    public ResponseEntity<ApiResponse<ChattingRoomCommandResponse>> createGroupChattingRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody GroupChattingRoomCreateRequest request
            ){
        ChattingRoomCommandResponse chattingRoomCommandResponse =
                chattingRoomCommandService.createGroupChattingRoom(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(chattingRoomCommandResponse));
    }

    @Operation(summary = "그룹 채팅방 제목 수정", description = "그룹 채팅방 제목을 수정한다.")
    @PutMapping("/chatting/update/group")
    public ResponseEntity<ApiResponse<Void>> updateGroupChattingRoom(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody GroupChattingRoomUpdateRequest request
    ){
        chattingRoomCommandService.updateChattingRoomTitle(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나가는 기능이다.")
    @PutMapping("/chatting/list/{chattingRoomId}")
    public ResponseEntity<ApiResponse<Void>> quitChattingRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int chattingRoomId
    ){
        chattingRoomCommandService.quitChattingRoom(userDetails.getUsername(),chattingRoomId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "채팅방 삭제", description = "채팅방 ID를 통해 채팅방을 삭제한다.")
    @DeleteMapping("/chatting/list/{chattingRoomId}")
    public ResponseEntity<ApiResponse<Void>> deleteChattingRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int chattingRoomId
    ){
        chattingRoomCommandService.deleteChattingRoom(userDetails.getUsername(),chattingRoomId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
