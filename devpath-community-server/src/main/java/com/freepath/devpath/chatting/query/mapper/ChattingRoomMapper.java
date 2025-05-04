package com.freepath.devpath.chatting.query.mapper;

import com.freepath.devpath.chatting.query.dto.response.ChattingRoomDTO;
import com.freepath.devpath.chatting.query.dto.response.ChattingRoomJoinUserDTO;
import com.freepath.devpath.chatting.query.dto.response.ChattingRoomWaitingDTO;
import com.freepath.devpath.chatting.query.dto.response.WaitingChattingRoomDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChattingRoomMapper {
    List<ChattingRoomDTO> selectChattingRooms(int userId);
    List<ChattingRoomWaitingDTO> selectWaitingUsers(int chattingRoomId);

    List<ChattingRoomJoinUserDTO> selectJoinUsers(int chattingRoomId);

    List<WaitingChattingRoomDTO> selectWaitingRoom(int userId);
}
