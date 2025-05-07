package com.freepath.devpath.chatting.command.application.service;

import com.freepath.devpath.board.post.command.domain.Board;
import com.freepath.devpath.board.post.command.repository.PostRepository;
import com.freepath.devpath.board.post.query.exception.NoSuchPostException;
import com.freepath.devpath.chatting.command.application.dto.request.GroupChattingRoomCreateRequest;
import com.freepath.devpath.chatting.command.application.dto.request.GroupChattingRoomUpdateRequest;
import com.freepath.devpath.chatting.command.application.dto.response.ChattingRoomCommandResponse;
import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingRole;
import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingRoom;
import com.freepath.devpath.chatting.command.domain.mongo.repository.ChattingRepository;
import com.freepath.devpath.chatting.command.domain.jpa.repository.ChattingRoomRepository;
import com.freepath.devpath.chatting.exception.ChattingRoomException;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.user.command.entity.User;
import com.freepath.devpath.user.command.repository.UserCommandRepository;
import com.freepath.devpath.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChattingRoomCommandService {

    private final ChattingJoinCommandService chattingJoinCommandService;
    private final ChattingRoomRepository chattingRoomRepository;
    private final UserCommandRepository userCommandRepository;
    private final ChattingRepository chattingRepository;
    private final PostRepository postRepository;
    private final ChattingCommandService chattingCommandService;

    @Transactional
    public ChattingRoomCommandResponse createChattingRoom(
            String username,
            int inviteeId
    ) {
        int creatorId = Integer.parseInt(username);
        Optional<Integer> optionalChattingRoomId = chattingRoomRepository.findValidChattingRoom(creatorId, inviteeId);
        //이미 생성되어 있는 일대일 채팅방의 경우 기존 채팅방 id반환
        if(optionalChattingRoomId.isPresent()){
            int chattingRoomId = optionalChattingRoomId.get();
            return new ChattingRoomCommandResponse(chattingRoomId);
        }
        ChattingRoom chattingRoom = ChattingRoom.builder()
                .userCount(2)
                .boardId(null)
                .build();
        ChattingRoom savedChattingRoom = chattingRoomRepository.save(chattingRoom);
        int chattingRoomId = savedChattingRoom.getChattingRoomId();

        chattingJoinCommandService.insert(creatorId, chattingRoomId, ChattingRole.ONE,'Y');
        chattingJoinCommandService.insert(inviteeId, chattingRoomId,ChattingRole.ONE,'Y');
        return new ChattingRoomCommandResponse(chattingRoomId);
    }

    @Transactional
    public ChattingRoomCommandResponse createGroupChattingRoom(
            String username, GroupChattingRoomCreateRequest request
    ) {
        int boardId = request.getBoardId();
        int userId = Integer.parseInt(username);
        Board board;
        //유효성 검사
        board = postRepository.findById(boardId).orElseThrow(
                () -> new NoSuchPostException(ErrorCode.POST_NOT_FOUND)
        );
        if(board.getUserId()!=userId){
            throw new ChattingRoomException(ErrorCode.NO_CHATTING_ROOM_AUTH);
        }
        if(chattingRoomRepository.findByBoardId(boardId).isPresent()){
            throw new ChattingRoomException(ErrorCode.CHATTING_ROOM_ALREADY_EXISTS);
        }
        //그룹채팅방 생성
        ChattingRoom chattingRoom = ChattingRoom.builder()
                .userCount(1)
                .boardId(boardId)
                .build();
        if(request.getChattingRoomTitle()==null) {
            chattingRoom.setChattingRoomTitle(board.getBoardTitle()+" 채팅방");
        }else{
            chattingRoom.setChattingRoomTitle(request.getChattingRoomTitle());
        }
        ChattingRoom savedChattingRoom = chattingRoomRepository.save(chattingRoom);
        int chattingRoomId = savedChattingRoom.getChattingRoomId();
        int creatorId = Integer.parseInt(username);
        chattingJoinCommandService.insert(creatorId, chattingRoomId,ChattingRole.OWNER,'Y');
        return new ChattingRoomCommandResponse(chattingRoomId);
    }

    @Transactional
    public void quitChattingRoom(String username, int chattingRoomId) {
        int userId = Integer.parseInt(username);
        //유효성 검사
        ChattingRoom chattingRoom = chattingRoomRepository.findById(chattingRoomId)
                .orElseThrow(() -> new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM));
        //퇴장 처리
        chattingJoinCommandService.setQuit(chattingRoomId,userId);
        User user = userCommandRepository.findById((long)userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        chattingRoom.setUserCount(chattingRoom.getUserCount()-1);
        String message = user.getNickname()+"님이 퇴장했습니다.";
       chattingCommandService.sendSystemMessage(chattingRoomId,message);
    }

    @Transactional
    public void deleteChattingRoom(String username, int chattingRoomId) {
        int userId = Integer.parseInt(username);
        chattingJoinCommandService.checkOwner(chattingRoomId,userId);
        ChattingRoom chattingRoom = chattingRoomRepository.findById(chattingRoomId)
                .orElseThrow(() -> new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM));
        chattingRoom.setBoardId(null);
        chattingJoinCommandService.deleteByChattingRoomId(chattingRoomId);
        chattingRepository.deleteByChattingRoomId(chattingRoomId);
        chattingRoomRepository.delete(chattingRoom);
    }

    @Transactional
    public void updateChattingRoomTitle(String username, GroupChattingRoomUpdateRequest request) {
        int userId = Integer.parseInt(username);
        int chattingRoomId = request.getChattingRoomId();
        String title = request.getChattingRoomTitle();
        //유효성 검사
        if(title==null || title.isBlank()){
            throw new ChattingRoomException(ErrorCode.INVALID_TITLE);
        }
        ChattingRoom chattingRoom = chattingRoomRepository.findById(chattingRoomId)
                .orElseThrow(() -> new ChattingRoomException(ErrorCode.NO_SUCH_CHATTING_ROOM));
        chattingJoinCommandService.checkOwner(chattingRoomId,userId);
        //제목 수정
        chattingRoom.setChattingRoomTitle(title);
    }
}
