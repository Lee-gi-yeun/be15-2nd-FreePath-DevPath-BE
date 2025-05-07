package com.freepath.devpath.chatting.command.application.service;

import com.freepath.devpath.chatting.command.application.dto.request.WaitingRoomActionRequest;
import com.freepath.devpath.chatting.command.application.dto.request.WaitingRoomAction;
import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingRoom;
import com.freepath.devpath.chatting.command.domain.jpa.repository.ChattingRoomRepository;
import com.freepath.devpath.chatting.exception.ChattingRoomException;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.user.command.entity.User;
import com.freepath.devpath.user.command.repository.UserCommandRepository;
import com.freepath.devpath.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingRoomCommandService {
    private final ChattingRoomRepository chattingRoomRepository;
    private final UserCommandRepository userCommandRepository;
    private final ChattingJoinCommandService chattingJoinCommandService;
    private final ChattingCommandService chattingCommandService;
    @Transactional
    public void joinRequest(int boardId, String username) {
        int userId = Integer.parseInt(username);

        ChattingRoom chattingRoom = chattingRoomRepository.findByBoardId(boardId).orElseThrow(
                () -> new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM)
        );
        if(chattingRoom.getBoardId() == null){
            throw new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM);
        }
        chattingJoinCommandService.setWaitingStatus(chattingRoom.getChattingRoomId(),userId);
    }
    @Transactional
    public void requestRespond(WaitingRoomActionRequest request, String username) {
        int userId = Integer.parseInt(username);
        int chattingRoomId = request.getChattingRoomId();
        int inviteeId = request.getInviteeId();
        //방장 권한 조회
        chattingJoinCommandService.checkOwner(chattingRoomId,userId);
        ChattingRoom chattingRoom = chattingRoomRepository.findById(chattingRoomId)
                .orElseThrow(
                        (() -> new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM))
                );
        // 참여 요청상태 확인
        chattingJoinCommandService.checkWaitingStatus(chattingRoomId, inviteeId);
        //요청 수락
        if(request.getAction().equals(WaitingRoomAction.ACCEPT)){
            chattingJoinCommandService.setStatus(chattingRoomId, inviteeId, 'Y');
            chattingRoom.setUserCount(chattingRoom.getUserCount()+1);
            User user = userCommandRepository.findById((long)request.getInviteeId())
                    .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
            //채팅 저장 및 전송
            String message = user.getNickname()+"님이 입장했습니다.";
            chattingCommandService.sendSystemMessage(chattingRoomId, message);

        }//요청 거절
        else if(request.getAction().equals(WaitingRoomAction.REJECT)){
            chattingJoinCommandService.setStatus(chattingRoomId, inviteeId, 'N');
        }

    }
}
