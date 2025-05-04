package com.freepath.devpath.chatting.query.controller;

import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingRoom;
import com.freepath.devpath.chatting.query.dto.response.ChattingRoomJoinUsersResponse;
import com.freepath.devpath.chatting.query.dto.response.ChattingRoomResponse;
import com.freepath.devpath.chatting.query.dto.response.GroupChattingWaitingRoomResponse;
import com.freepath.devpath.chatting.query.dto.response.WaitingChattingRoomResponse;
import com.freepath.devpath.chatting.query.service.ChattingRoomQueryService;
import com.freepath.devpath.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "채팅방 조회", description = "채팅방 조회 API")
public class ChattingRoomQueryController {
    private final ChattingRoomQueryService chattingRoomQueryService;

    @Operation(summary = "채팅방 리스트 조회", description = "특정 사용자가 참여중인 채팅방 리스트를 조회한다.")
    @GetMapping("/chatting/list")
    public ResponseEntity<ApiResponse<ChattingRoomResponse>> getChattingRooms(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        ChattingRoomResponse response = chattingRoomQueryService.getChattingRooms(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "참여대기방 조회", description = "방장만이 채팅 참여 대기방 인원을 조회한다.")
    @GetMapping("/chatting/waitingRoom/{chattingRoomId}")
    public ResponseEntity<ApiResponse<GroupChattingWaitingRoomResponse>> getWaitingRoom(
            @PathVariable int chattingRoomId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        GroupChattingWaitingRoomResponse groupChattingWaitingRoomResponse =
                chattingRoomQueryService.getWaitingRoom(chattingRoomId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(groupChattingWaitingRoomResponse));
    }

    @Operation(summary = "채팅방 참여인원 조회", description = "채팅방 id를 이용하여 채팅방에 참여중인 인원을 조회한다.")
    @GetMapping("/chatting/list/{chattingRoomId}/users")
    public ResponseEntity<ApiResponse<ChattingRoomJoinUsersResponse>> getChattingRoomJoinUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int chattingRoomId
    ){
        ChattingRoomJoinUsersResponse response =chattingRoomQueryService.getChattingRoomJoinUsers(chattingRoomId,userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "참여 대기중인 채팅방 조회", description = "특정 사용자가 참여 요청르 보낸 채팅방 목록을 조회한다.")
    @GetMapping("/chatting/waitingChattingRoom")
    public ResponseEntity<ApiResponse<WaitingChattingRoomResponse>> getWaitingChattingRoom(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        WaitingChattingRoomResponse response = chattingRoomQueryService.getWaitingChattingRoom(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
