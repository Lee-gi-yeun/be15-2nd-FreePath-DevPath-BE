package com.freepath.devpath.board.post.command.service;

import com.freepath.devpath.board.post.command.domain.BoardDocument;
import com.freepath.devpath.board.post.command.dto.request.PostCreateRequest;
import com.freepath.devpath.board.post.command.dto.request.PostUpdateRequest;
import com.freepath.devpath.board.post.command.domain.Board;
import com.freepath.devpath.board.post.command.exception.FileDeleteFailedException;
import com.freepath.devpath.board.post.command.exception.FileUpdateFailedException;
import com.freepath.devpath.board.post.command.exception.InvalidPostAuthorException;
import com.freepath.devpath.board.post.command.exception.NoSuchPostException;
import com.freepath.devpath.board.post.command.repository.PostElasticRepository;
import com.freepath.devpath.board.post.command.repository.PostRepository;
import com.freepath.devpath.board.post.command.dto.response.PostCreateResponse;
import com.freepath.devpath.board.vote.command.service.VoteCommandService;
import com.freepath.devpath.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostCommandService {
    private final PostRepository postRepository;
    private final PostElasticRepository postElasticRepository;
    private final AttachmentService attachmentService;
    private final VoteCommandService voteCommandService;

    @Transactional
    public PostCreateResponse createPost(PostCreateRequest postCreateRequest, List<MultipartFile> multipartFiles, int userId) {
        // 게시글 저장
        Board board = Board.builder()
                .userId(userId)
                .boardCategory(postCreateRequest.getBoardCategory())
                .boardTitle(postCreateRequest.getBoardTitle())
                .boardContents(postCreateRequest.getBoardContents())
                .build();
        Board saved = postRepository.save(board);

        // Elasticsearch 동기화
        postElasticRepository.save(BoardDocument.builder()
                .boardId(String.valueOf(saved.getBoardId()))
                .boardTitle(postCreateRequest.getBoardTitle())
                .boardContents(postCreateRequest.getBoardContents())
                .createdAt(saved.getBoardCreatedAt())
                .build());

        // 임시 이미지 이동 처리
        if (postCreateRequest.getUsedImageUrls() != null) {
            attachmentService.migrateTempImages(postCreateRequest.getUsedImageUrls(), saved.getBoardId(), userId);
        }

        // 투표 처리
        if (postCreateRequest.getVote() != null) {
            voteCommandService.createVote(postCreateRequest.getVote(), saved.getBoardId());
        }

        return PostCreateResponse.builder()
                .postId(saved.getBoardId())
                .build();
    }


    public Map<String, String> uploadTempImage(MultipartFile image, int userId) {
        return attachmentService.uploadTempImage(image, userId);
    }


    @Transactional
    public void deletePost(int boardId, int userId) {
        Board post = postRepository.findById(boardId)
                .orElseThrow(() -> new NoSuchPostException(ErrorCode.POST_DELETE_FAILED));

        // 작성자 검증
        if (post.getUserId() != userId) {
            throw new InvalidPostAuthorException(ErrorCode.POST_DELETE_FORBIDDEN);
        }

        // 이미 삭제된 경우
        if (post.getIsBoardDeleted() == 'Y') {
            throw new FileDeleteFailedException(ErrorCode.POST_ALREADY_DELETED);
        }

        // 이미 신고되어있는 게시물을 삭제하려는 경우 에러 반환
        if (post.getIsBoardDeleted() == 'R') {
            throw new FileUpdateFailedException(ErrorCode.POST_ALREADY_REPORTED);
        }

        // 첨부파일 삭제 요청
        attachmentService.deleteAttachmentsByBoardId(boardId);

        // 게시글 Soft Delete
        post.delete();
    }

    @Transactional
    public void updatePost(PostUpdateRequest postUpdateRequest, int boardId, int userId) {
        Board post = postRepository.findById(boardId)
                .orElseThrow(() -> new NoSuchPostException(ErrorCode.POST_NOT_FOUND));

        // 다른 회원이 해당 게시물 ID를 알고 수정하려는 경우를 필터링
        if (post.getUserId() != userId) {
            throw new InvalidPostAuthorException(ErrorCode.POST_UPDATE_FORBIDDEN);
        }

        // 이미 삭제되어있는 게시물을 수정하려는 경우 에러 반환
        if (post.getIsBoardDeleted() == 'Y') {
            throw new FileUpdateFailedException(ErrorCode.POST_ALREADY_DELETED);
        }

        // 이미 신고되어있는 게시물을 수정하려는 경우 에러 반환
        if (post.getIsBoardDeleted() == 'R') {
            throw new FileUpdateFailedException(ErrorCode.POST_ALREADY_REPORTED);
        }

        String modifiedTitle = postUpdateRequest.getTitle();
        String modifiedContents = postUpdateRequest.getContent();

        // 게시글 제목, 내용 수정
        post.modifyTitleAndContent(modifiedTitle, modifiedContents);

        // Elasticsearch에 동기화
        BoardDocument elasticPost = BoardDocument.builder()
                .boardId(String.valueOf(boardId))
                .boardTitle(modifiedTitle)
                .boardContents(modifiedContents)
                .createdAt(post.getBoardCreatedAt())
                .build();

        postElasticRepository.save(elasticPost);
    }

    @Transactional
    public void updatePostDeletedStatus(Integer postId, char checkResult) {
        // 게시글 조회
        Board post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchPostException(ErrorCode.POST_NOT_FOUND));

        // 삭제 상태를 'Y' 또는 'N'에 맞게 업데이트
        if (checkResult == 'Y') {
            post.delete(); // 삭제 처리
        } else if (checkResult == 'N') {
            post.restore(); // 복구 처리
        }

        // JPA dirty checking을 통해 자동으로 업데이트됨
    }
}

