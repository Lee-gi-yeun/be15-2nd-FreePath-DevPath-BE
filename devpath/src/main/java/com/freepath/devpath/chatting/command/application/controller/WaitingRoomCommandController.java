package com.freepath.devpath.chatting.command.application.controller;

import com.freepath.devpath.chatting.command.application.dto.request.WaitingRoomActionRequest;
import com.freepath.devpath.chatting.command.application.service.WaitingRoomCommandService;
import com.freepath.devpath.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "그룹채팅방 대기방 참여 관리", description = "그룹채팅방 대기방 참여 생성, 삭제 API")
public class WaitingRoomCommandController {
    private final WaitingRoomCommandService waitingRoomCommandService;

    @Operation(summary = "그룹채팅방 참여 요청", description = "그룹채팅방id를 이용하여 참여 요청을 보낸다")
    @PostMapping("/chatting/waitingRoom/{boardId}")
    public ResponseEntity<ApiResponse<Void>> groupChattingJoin(
            @PathVariable int boardId,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        waitingRoomCommandService.joinRequest(boardId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "그룹채팅방 참여 요청 응답", description = "그룹채팅방 참여 요청에 대해 채팅방 OWNER 가 처리한다.")
    @PutMapping("/chatting/waitingRoom/respond")
    public ResponseEntity<ApiResponse<Void>> groupChattingAccept(
            @RequestBody WaitingRoomActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        waitingRoomCommandService.requestRespond(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
