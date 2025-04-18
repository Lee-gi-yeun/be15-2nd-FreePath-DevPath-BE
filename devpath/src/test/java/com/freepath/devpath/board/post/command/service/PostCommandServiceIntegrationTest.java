package com.freepath.devpath.board.post.command.service;

import com.freepath.devpath.board.post.command.domain.Board;
import com.freepath.devpath.board.post.command.domain.BoardDocument;
import com.freepath.devpath.board.post.command.dto.request.PostCreateRequest;
import com.freepath.devpath.board.post.command.dto.request.PostUpdateRequest;
import com.freepath.devpath.board.post.command.dto.response.PostCreateResponse;
import com.freepath.devpath.board.post.command.exception.FileUpdateFailedException;
import com.freepath.devpath.board.post.command.exception.InvalidPostAuthorException;
import com.freepath.devpath.board.post.command.repository.PostElasticRepository;
import com.freepath.devpath.board.post.command.repository.PostRepository;
import com.freepath.devpath.board.vote.command.dto.request.VoteCreateRequest;
import com.freepath.devpath.board.vote.command.entity.Vote;
import com.freepath.devpath.board.vote.command.entity.VoteItem;
import com.freepath.devpath.board.vote.command.repository.VoteItemRepository;
import com.freepath.devpath.board.vote.command.repository.VoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostCommandServiceIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostElasticRepository postElasticRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private VoteItemRepository voteItemRepository;

    @Autowired
    private PostCommandService postCommandService;

    // 이미 존재하는 user를 기준으로 작업
    private final int userId = 1;
    @Test
    @DisplayName("게시글 생성")
    void createPost() {
        // given
        VoteCreateRequest voteRequest = new VoteCreateRequest();
        ReflectionTestUtils.setField(voteRequest, "voteTitle", "점심 뭐 먹을래?");
        ReflectionTestUtils.setField(voteRequest, "voteDuedate", LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(voteRequest, "options", List.of("김밥", "햄버거"));

        PostCreateRequest request = PostCreateRequest.builder()
                .boardCategory(1)
                .boardTitle("투표 포함 게시글")
                .boardContents("투표 포함 게시글 내용입니다.")
                .vote(voteRequest)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test data".getBytes()
        );

        // when
        PostCreateResponse response = postCommandService.createPost(
                request,
                List.of(file),
                userId
        );

        // then
        Board board = postRepository.findById(response.getPostId()).orElse(null);
        assertThat(board).isNotNull();
        assertThat(board.getBoardCategory()).isEqualTo(1);
        assertThat(board.getBoardTitle()).isEqualTo("투표 포함 게시글");
        assertThat(board.getBoardContents()).isEqualTo("투표 포함 게시글 내용입니다.");
        assertThat(board.getBoardModifiedAt()).isNull();

        // Elasticsearch 확인
        BoardDocument document = postElasticRepository.findById(String.valueOf(board.getBoardId())).orElse(null);
        assertThat(document).isNotNull();
        assertThat(document.getBoardTitle()).isEqualTo("투표 포함 게시글");
        assertThat(document.getBoardContents()).isEqualTo("투표 포함 게시글 내용입니다.");

        Vote vote = voteRepository.findByBoardId(response.getPostId()).orElse(null);
        assertThat(vote).isNotNull();
        assertThat(vote.getVoteTitle()).isEqualTo("점심 뭐 먹을래?");
        assertThat(vote.getIsVoteFinished()).isEqualTo('N');

        List<VoteItem> voteItemList = voteItemRepository.findByVoteId(vote.getVoteId());
        assertThat(voteItemList.size()).isEqualTo(2);
        assertThat(voteItemList.get(0).getVoteCount()).isEqualTo(0);
        assertThat(voteItemList.get(1).getVoteCount()).isEqualTo(0);
        assertThat(voteItemList.get(0).getVoteItemTitle()).isEqualTo("김밥");
        assertThat(voteItemList.get(1).getVoteItemTitle()).isEqualTo("햄버거");
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {
        // given
        Board board = Board.builder()
                .userId(userId)
                .boardTitle("원래 제목")
                .boardContents("원래 내용")
                .boardCategory(1)
                .isBoardDeleted('N')
                .build();
        postRepository.save(board);

        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        // when
        postCommandService.updatePost(updateRequest, board.getBoardId(), userId);

        // then
        Board updated = postRepository.findById(board.getBoardId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getBoardTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getBoardContents()).isEqualTo("수정된 내용");

        BoardDocument document = postElasticRepository.findById(String.valueOf(board.getBoardId())).orElse(null);
        assertThat(document).isNotNull();
        assertThat(document.getBoardTitle()).isEqualTo("수정된 제목");
        assertThat(document.getBoardContents()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        // given
        Board board = Board.builder()
                .userId(userId)
                .boardTitle("삭제할 게시글")
                .boardContents("삭제 대상입니다.")
                .boardCategory(1)
                .isBoardDeleted('N')
                .build();
        postRepository.save(board);

        // when
        postCommandService.deletePost(board.getBoardId(), userId);

        // then
        Board deleted = postRepository.findById(board.getBoardId()).orElse(null);
        assertThat(deleted).isNotNull();
        assertThat(deleted.getIsBoardDeleted()).isEqualTo('Y');
    }

    @Test
    @DisplayName("게시글 수정 - 삭제된 게시물 수정 시도")
    void updatePost_deletedPost() {
        Board board = Board.builder()
                .userId(userId)
                .boardTitle("삭제된 게시물")
                .boardContents("수정 시도")
                .boardCategory(1)
                .isBoardDeleted('Y')
                .build();
        postRepository.save(board);

        PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        // when & then
        assertThatThrownBy(() ->
                postCommandService.updatePost(updateRequest, board.getBoardId(), userId)
        ).isInstanceOf(FileUpdateFailedException.class);
    }

    @Test
    @DisplayName("게시글 삭제 - 작성자 불일치")
    void deletePost_invalidAuthor() {
        Board board = Board.builder()
                .userId(userId)
                .boardTitle("삭제 테스트")
                .boardContents("작성자 다름")
                .boardCategory(1)
                .isBoardDeleted('N')
                .build();
        postRepository.save(board);

        // when & then
        assertThatThrownBy(() ->
                postCommandService.deletePost(board.getBoardId(), userId + 1)
        ).isInstanceOf(InvalidPostAuthorException.class);
    }
}
