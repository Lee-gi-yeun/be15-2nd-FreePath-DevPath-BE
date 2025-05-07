package com.freepath.devpath.chatting.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChattingDTO {
    private int userId;
    private String nickname;
    private String message;
    private String timestamp;
    private boolean isMine;
}
