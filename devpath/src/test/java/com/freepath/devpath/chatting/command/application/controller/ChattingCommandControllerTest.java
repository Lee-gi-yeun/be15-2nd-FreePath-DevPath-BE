package com.freepath.devpath.chatting.command.application.controller;

import com.freepath.devpath.chatting.command.application.dto.response.ChattingListResponse;
import com.freepath.devpath.chatting.command.domain.mongo.aggregate.Chatting;
import com.freepath.devpath.chatting.query.dto.response.ChattingDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

    import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ChattingCommandControllerTest {


    @Test
    @DisplayName("채팅 리스트 조회")
    void loadChattingList_ReturnsChattingListResponse() {
        // given
        List<Chatting> chattingList = List.of(
                new Chatting(1,1, "안녕하세요", LocalDateTime.of(2024, 4, 15, 10, 0)),
                new Chatting(1,2, "반갑습니다", LocalDateTime.of(2024, 4, 15, 10, 5))
        );

        Map<Integer, String> userIdToNickname = Map.of(
                1, "철수",
                2, "영희"
        );

        // when
        List<ChattingDTO> chattingDTOList = chattingList.stream()
                .map(chat -> new ChattingDTO(
                        chat.getUserId(),
                        userIdToNickname.getOrDefault(chat.getUserId(), "알 수 없음"),
                        chat.getChattingMessage(),
                        chat.getChattingCreatedAt().toString()
                ))
                .toList();

        ChattingListResponse response = new ChattingListResponse(chattingDTOList);

        // then
        assertEquals(2, response.getChattingList().size());

        ChattingDTO first = response.getChattingList().get(0);
        assertEquals(1L, first.getUserId());
        assertEquals("철수", first.getNickname());
        assertEquals("안녕하세요", first.getMessage());

        ChattingDTO second = response.getChattingList().get(1);
        assertEquals("영희", second.getNickname());
    }
}