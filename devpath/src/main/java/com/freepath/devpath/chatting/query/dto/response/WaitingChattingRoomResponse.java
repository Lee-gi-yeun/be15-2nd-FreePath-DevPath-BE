package com.freepath.devpath.chatting.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WaitingChattingRoomResponse {
    private List<WaitingChattingRoomDTO> waitingChattingRoomDTOList;
}
