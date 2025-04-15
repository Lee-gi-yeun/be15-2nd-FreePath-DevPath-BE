package com.freepath.devpath.chatting.command.application.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freepath.devpath.chatting.command.application.dto.request.GroupChattingRoomCreateRequest;
import com.freepath.devpath.chatting.command.application.dto.request.GroupChattingRoomUpdateRequest;
import com.freepath.devpath.chatting.command.application.dto.response.ChattingRoomCommandResponse;
import com.freepath.devpath.chatting.command.application.service.ChattingRoomCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = ChattingRoomCommandController.class)
class ChattingRoomCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ChattingRoomCommandService chattingRoomCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user01")
    @DisplayName("일대일 채팅방 생성")
    void createChattingRoom() throws Exception {
        int userId = 2;
        ChattingRoomCommandResponse mockResponse = new ChattingRoomCommandResponse(1);
        Mockito.when(chattingRoomCommandService.createChattingRoom(eq("username"), eq(userId)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/chatting/create/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("그룹 채팅방 생성")
    void createGroupChattingRoom() throws Exception {
        GroupChattingRoomCreateRequest request = new GroupChattingRoomCreateRequest(
                42,"그룹채팅방");
        ChattingRoomCommandResponse mockResponse = new ChattingRoomCommandResponse(1);
        Mockito.when(chattingRoomCommandService.createGroupChattingRoom(eq("user01"), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/chatting/create/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("그룹 채팅방 제목 수정")
    void updateGroupChattingRoom() throws Exception {
        GroupChattingRoomUpdateRequest request = new GroupChattingRoomUpdateRequest(
                101,"새로운 채팅방 제목"
        );
        mockMvc.perform(put("/chatting/update/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }


    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("채팅방 나가기")
    void quitChattingRoom() throws Exception {
        int chattingRoomId = 123;

        mockMvc.perform(put("/chatting/list/{chattingRoomId}", chattingRoomId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("채팅방 삭제")
    void deleteChattingRoom() throws Exception {
        int chattingRoomId = 123;

        mockMvc.perform(delete("/chatting/list/{chattingRoomId}", chattingRoomId))
                .andExpect(status().isOk());
    }
}
