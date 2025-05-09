package com.freepath.devpath.board.comment.command.application.service;

import com.freepath.devpath.board.comment.command.application.dto.CommentResponseDto;
import com.freepath.devpath.board.comment.command.domain.domain.Comment;
import com.freepath.devpath.board.comment.command.application.dto.CommentRequestDto;
import com.freepath.devpath.board.comment.command.domain.repository.CommentRepository;
import com.freepath.devpath.board.comment.command.exception.CommentAccessDeniedException;
import com.freepath.devpath.board.comment.command.exception.CommentDeleteDeniedException;
import com.freepath.devpath.board.comment.command.exception.CommentInvalidArgumentException;
import com.freepath.devpath.board.comment.command.exception.CommentNotFoundException;
import com.freepath.devpath.board.comment.query.exception.NoSuchCommentException;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.user.command.repository.UserCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final UserCommandRepository userCommandRepository;

    @Transactional
    public CommentResponseDto saveComment(CommentRequestDto dto, int userId) {
        Integer boardId = dto.getBoardId();
        Integer parentCommentId = dto.getParentCommentId();

        // 댓글과 대댓글을 동시에 지정하면 오류
        if ((boardId == null && parentCommentId == null) || (boardId != null && parentCommentId != null)) {
            throw new CommentInvalidArgumentException(ErrorCode.COMMENT_INVALID_ARGUMENT);
        }

        if (parentCommentId != null) {
            // 대댓글인 경우, 부모 댓글 가져오기
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CommentNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

            // 부모 댓글도 대댓글인 경우 → 대대댓글 → 허용 X
            if (parentComment.getParentCommentId() != null) {
                throw new CommentInvalidArgumentException(ErrorCode.COMMENT_INVALID_ARGUMENT);
            }

            // 부모 댓글의 boardId로 설정
            boardId = parentComment.getBoardId();
        }

        Comment comment = Comment.builder()
                .userId(userId)
                .boardId(boardId)
                .parentCommentId(parentCommentId)
                .contents(dto.getContents())
                .createdAt(new Date())
                .deleted('N')
                .build();

        Comment saved = commentRepository.save(comment);

        // ✅ 닉네임 조회
        String nickname = userCommandRepository.findNicknameByUserId(userId)
                .orElse("알 수 없음"); // orElseThrow로 수정 요망

        // ✅ DTO 변환
        return CommentResponseDto.builder()
                .commentId(saved.getCommentId())
                .userId(saved.getUserId())
                .boardId(saved.getBoardId())
                .parentCommentId(saved.getParentCommentId())
                .contents(saved.getContents())
                .createdAt(saved.getCreatedAt())
                .modifiedAt(saved.getModifiedAt())
                .deleted(saved.getDeleted())
                .nickname(nickname)
                .build();
    }

    @Transactional
    public void updateComment(int commentId, String newContent, int userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getUserId() != userId) {
            throw new CommentAccessDeniedException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        if (Character.valueOf('Y').equals(comment.getDeleted())) {
            throw new CommentAccessDeniedException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.updateContent(newContent);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(int commentId, int userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getUserId() != userId) {
            throw new CommentDeleteDeniedException(ErrorCode.COMMENT_DELETE_DENIED);
        }

        if (Character.valueOf('Y').equals(comment.getDeleted())) {
            throw new CommentAccessDeniedException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.softDelete();
    }

    @Transactional
    public void updateCommentDeletedStatus(Integer commentId, char checkResult) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchCommentException(ErrorCode.COMMENT_NOT_FOUND));

        // 삭제 상태를 'Y' 또는 'N'에 맞게 업데이트
        if (checkResult == 'Y') {
            comment.softDelete(); // 삭제 처리
        } else if (checkResult == 'N') {
            comment.restore(); // 복구 처리
        }

        // JPA dirty checking을 통해 자동으로 업데이트됨
    }
}
