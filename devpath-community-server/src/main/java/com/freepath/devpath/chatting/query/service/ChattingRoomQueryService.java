package com.freepath.devpath.chatting.query.service;

import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingJoin;
import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingJoinId;
import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingRole;
import com.freepath.devpath.chatting.command.domain.jpa.repository.ChattingJoinRepository;
import com.freepath.devpath.chatting.command.domain.jpa.repository.ChattingRoomRepository;
import com.freepath.devpath.chatting.exception.ChattingJoinException;
import com.freepath.devpath.chatting.exception.ChattingRoomException;
import com.freepath.devpath.chatting.query.dto.response.*;
import com.freepath.devpath.chatting.query.mapper.ChattingRoomMapper;
import com.freepath.devpath.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChattingRoomQueryService {
    private final ChattingRoomMapper chattingRoomMapper;
    private final ChattingJoinRepository chattingJoinRepository;
    private final ChattingRoomRepository chattingRoomRepository;

    @Transactional
    public ChattingRoomResponse getChattingRooms(String username) {
        int userId = Integer.parseInt(username);
        List<ChattingRoomDTO> chattingRooms = chattingRoomMapper.selectChattingRooms(userId);
        return ChattingRoomResponse.builder()
                .chattingRooms(chattingRooms)
                .build();
    }
    @Transactional
    public GroupChattingWaitingRoomResponse getWaitingRoom(int chattingRoomId, String username) {
        int userId = Integer.parseInt(username);
        if(!chattingRoomRepository.existsById(chattingRoomId)){
            throw new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM);
        }
        ChattingJoin chattingJoin = chattingJoinRepository.findById(new ChattingJoinId(chattingRoomId,userId)).orElseThrow(
                ()-> new ChattingJoinException(ErrorCode.NO_CHATTING_JOIN)
        );
        if(!chattingJoin.getChattingRole().equals(ChattingRole.OWNER)){
            throw new ChattingJoinException(ErrorCode.NO_CHATTING_ROOM_AUTH);
        }
        List<ChattingRoomWaitingDTO> chattingRoomWaitingDTOS = chattingRoomMapper.selectWaitingUsers(chattingRoomId);

        return GroupChattingWaitingRoomResponse.builder()
                .chattingRoomWatingDTOList(chattingRoomWaitingDTOS)
                .build();

    }

    @Transactional(readOnly = true)
    public ChattingRoomJoinUsersResponse getChattingRoomJoinUsers(int chattingRoomId, String username) {
        int userId = Integer.parseInt(username);
        if(!chattingRoomRepository.existsById(chattingRoomId)){
            throw new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM);
        }
        if(!chattingJoinRepository.existsById(new ChattingJoinId(chattingRoomId, userId))){
            throw new ChattingJoinException(ErrorCode.NO_CHATTING_JOIN);
        }
        List<ChattingRoomJoinUserDTO> joinUsers = chattingRoomMapper.selectJoinUsers(chattingRoomId);
        return new ChattingRoomJoinUsersResponse(joinUsers);
    }

    public WaitingChattingRoomResponse getWaitingChattingRoom(String username) {
        int userId = Integer.parseInt(username);
        List<WaitingChattingRoomDTO> waitingChattingRooms = chattingRoomMapper.selectWaitingRoom(userId);
        return new WaitingChattingRoomResponse(waitingChattingRooms);
    }
}
