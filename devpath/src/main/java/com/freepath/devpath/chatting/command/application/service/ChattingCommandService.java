package com.freepath.devpath.chatting.command.application.service;

import com.freepath.devpath.chatting.command.application.dto.response.ChattingResponse;
import com.freepath.devpath.chatting.command.domain.mongo.repository.ChattingRepository;
import com.freepath.devpath.chatting.command.application.dto.request.ChattingRequest;
import com.freepath.devpath.chatting.command.domain.mongo.aggregate.Chatting;
import com.freepath.devpath.chatting.command.domain.jpa.repository.ChattingRoomRepository;
import com.freepath.devpath.chatting.exception.ChattingRoomException;
import com.freepath.devpath.chatting.exception.InvalidMessageException;
import com.freepath.devpath.chatting.query.dto.response.ChattingDTO;
import com.freepath.devpath.chatting.command.application.dto.response.ChattingListResponse;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.user.command.entity.User;
import com.freepath.devpath.user.command.repository.UserCommandRepository;
import com.freepath.devpath.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChattingCommandService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChattingRepository chattingRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final UserCommandRepository userCommandRepository;
    private final ChattingJoinCommandService chattingJoinCommandService;


    @Transactional
    public void sendChatting(ChattingRequest chattingRequest, int userId) {
        //유효성 검사
        //유효한 채팅방인지
        if(chattingRequest.getChattingRoomId()!=null && !chattingRoomRepository.existsById(chattingRequest.getChattingRoomId())){
            throw new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM);
        }
        //채팅방에 참여중인 유저인지
        chattingJoinCommandService.checkStatus(chattingRequest.getChattingRoomId(),userId);
        //유효한 메세지인지
        if(chattingRequest.getChattingMessage()==null){
            throw new InvalidMessageException(ErrorCode.INVALID_MESSAGE);
        }
        send(chattingRequest, userId);
    }

    @Transactional
    public void sendSystemMessage(int chattingRoomId, String message){
        send(ChattingRequest.builder()
                .chattingMessage(message)
                .chattingRoomId(chattingRoomId)
                .build(),1);
    }


    @Transactional
    public void send(ChattingRequest chattingRequest, int userId) {
        Chatting chatting = Chatting.builder()
                .chattingRoomId(chattingRequest.getChattingRoomId())
                .userId(userId)
                .chattingMessage(chattingRequest.getChattingMessage())
                .chattingCreatedAt(LocalDateTime.now())
                .build();

        Chatting savedChatting = chattingRepository.save(chatting);
        User user = userCommandRepository.findById((long) userId).orElseThrow(
                () -> new UserException(ErrorCode.USER_NOT_FOUND)
        );

        ChattingResponse chattingResponse = ChattingResponse.builder()
                .userId(userId)
                .message(savedChatting.getChattingMessage())
                .timestamp(savedChatting.getChattingCreatedAt().toString())
                .nickname(user.getNickname())
                        .build();
        messagingTemplate.convertAndSend("/topic/room/" + chatting.getChattingRoomId(), chattingResponse);
    }

    @Transactional(readOnly = true)
    public ChattingListResponse getChattingList(int userId, int chattingRoomId) {
        //유효성 체크
        chattingRoomRepository.findById(chattingRoomId).orElseThrow(
                () -> new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM)
        );
        chattingJoinCommandService.checkStatus(chattingRoomId, userId);

        List<Chatting> chattingList = chattingRepository.findByChattingRoomId(chattingRoomId);

        // 중복 없는 userId만 추출
        Set<Integer> userIds = chattingList.stream()
                .map(Chatting::getUserId)
                .collect(Collectors.toSet());

        // MariaDB에서 사용자 정보 한 번에 조회
        List<User> users = userCommandRepository.findByUserIdIn(userIds);
        Map<Integer, String> userIdToNickname = users.stream()
                .collect(Collectors.toMap(User::getUserId, User::getNickname));

        //DTO 변환
        List<ChattingDTO> chattingDTOList =  chattingList.stream()
                .map(chat -> new ChattingDTO(
                        chat.getUserId(),
                        userIdToNickname.getOrDefault(chat.getUserId(), "알 수 없음"),
                        chat.getChattingMessage(),
                        chat.getChattingCreatedAt().toString(),
                        (userId==chat.getUserId())
                ))
                .collect(Collectors.toList());
        return new ChattingListResponse(chattingDTOList);
    }
}
