package com.freepath.devpath.board.post.command.service;

import com.freepath.devpath.board.post.command.domain.Board;
import com.freepath.devpath.board.post.command.domain.BoardDocument;
import com.freepath.devpath.board.post.command.dto.request.PostCreateRequest;
import com.freepath.devpath.board.post.command.dto.request.PostUpdateRequest;
import com.freepath.devpath.board.post.command.dto.response.PostCreateResponse;
import com.freepath.devpath.board.post.command.repository.PostElasticRepository;
import com.freepath.devpath.board.post.command.repository.PostRepository;
import com.freepath.devpath.board.vote.command.dto.request.VoteCreateRequest;
import com.freepath.devpath.board.vote.command.service.VoteCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostElasticRepository postElasticRepository;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private VoteCommandService voteCommandService;

    @InjectMocks
    private PostCommandService postCommandService;

    private final int userId = 1;
    private final int boardId = 100;

    @Test
    void createPost_success() {
        // given
        VoteCreateRequest voteRequest = new VoteCreateRequest();
        ReflectionTestUtils.setField(voteRequest, "voteTitle", "점심 뭐 먹을래?");
        ReflectionTestUtils.setField(voteRequest, "voteDuedate", LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(voteRequest, "options", List.of("김밥", "햄버거"));

        PostCreateRequest request = PostCreateRequest.builder()
                .boardTitle("제목")
                .boardContents("내용")
                .boardCategory(1)
                .vote(voteRequest)
                .build();

        List<MultipartFile> mockFiles = Collections.emptyList(); // 파일 없음

        Board board = Board.builder()
                .boardId(boardId)
                .userId(userId)
                .boardTitle(request.getBoardTitle())
                .boardContents(request.getBoardContents())
                .boardCategory(request.getBoardCategory())
                .build();

        given(postRepository.save(Mockito.<Board>any())).willReturn(board);

        // when
        PostCreateResponse response = postCommandService.createPost(request, mockFiles, userId);

        // then
        assertEquals(boardId, response.getPostId());

        // 게시글 저장 확인
        verify(postRepository).save(Mockito.<Board>any());

        // Elasticsearch 저장 확인
        verify(postElasticRepository).save(Mockito.<BoardDocument>any());

        // 파일 업로드 서비스 호출 확인
        verify(attachmentService).uploadAndSaveFiles(mockFiles, boardId, userId);

        // Elasticsearch에 저장된 내용 검증
        verify(postElasticRepository).save(argThat(postDocument ->
                postDocument.getBoardId().equals(String.valueOf(boardId)) &&
                        postDocument.getBoardTitle().equals(request.getBoardTitle()) &&
                        postDocument.getBoardContents().equals(request.getBoardContents())
        ));

        // 투표 생성 서비스 호출 확인
        verify(voteCommandService).createVote(
                argThat(vote ->
                        vote.getVoteTitle().equals("점심 뭐 먹을래?") &&
                                vote.getOptions().containsAll(List.of("김밥", "햄버거"))
                ),
                eq(boardId)
        );

    }

    @Test
    void deletePost_success() {
        // given
        Board post = Board.builder()
                .boardId(boardId)
                .userId(userId)
                .isBoardDeleted('N')
                .build();

        when(postRepository.findById(boardId)).thenReturn(Optional.of(post));

        // when
        postCommandService.deletePost(boardId, userId);

        // then
        verify(attachmentService).deleteAttachmentsByBoardId(boardId);
        assertEquals('Y', post.getIsBoardDeleted());
    }

    @Test
    void updatePost_success() {
        // given
        Board post = Board.builder()
                .boardId(boardId)
                .userId(userId)
                .boardTitle("Old Title")
                .boardContents("Old Content")
                .isBoardDeleted('N')
                .build();

        when(postRepository.findById(boardId)).thenReturn(Optional.of(post));

        PostUpdateRequest request = PostUpdateRequest.builder()
                .title("New Title")
                .content("New Content")
                .build();

        // when
        postCommandService.updatePost(request, boardId, userId);

        // then
        assertEquals("New Title", post.getBoardTitle());
        assertEquals("New Content", post.getBoardContents());
        verify(postElasticRepository).save(Mockito.<BoardDocument>any());
    }

    @Test
    void updatePostDeletedStatus_success() {
        // given
        Board post = Board.builder()
                .boardId(boardId)
                .userId(userId)
                .isBoardDeleted('R')
                .build();

        when(postRepository.findById(boardId)).thenReturn(Optional.of(post));

        // when
        postCommandService.updatePostDeletedStatus(boardId, 'Y');

        // then
        assertEquals('Y', post.getIsBoardDeleted());
    }
}
