package com.freepath.devpath.board.comment.query.service;

import com.freepath.devpath.board.comment.command.exception.CommentNotFoundException;
import com.freepath.devpath.board.comment.query.dto.*;
import com.freepath.devpath.board.comment.query.exception.NoSuchCommentException;
import com.freepath.devpath.board.comment.query.mapper.CommentMapper;
import com.freepath.devpath.common.dto.Pagination;
import com.freepath.devpath.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public List<CommentTreeDto> getCommentsAsTree(int boardId) {
        List<HierarchicalCommentDto> flatList = commentMapper.findHierarchicalComments(boardId);

        Map<Integer, CommentTreeDto> dtoMap = new HashMap<>();
        List<CommentTreeDto> result = new ArrayList<>();

        for (int i = 0; i < flatList.size(); i++) {
            HierarchicalCommentDto flat = flatList.get(i);

            String contents;
            if ("Y".equals(flat.getIsCommentDeleted())) {
                contents = "삭제된 댓글입니다.";
            } else if ("R".equals(flat.getIsCommentDeleted())) {
                contents = "신고된 댓글입니다.";
            } else {
                contents = flat.getContents();
            }


            CommentTreeDto node = CommentTreeDto.builder()
                    .userId(flat.getUserId())
                    .commentId(flat.getCommentId())
                    .nickname(flat.getNickname())
                    .contents(contents)
                    .createdAt(flat.getCreatedAt())
                    .modifiedAt(flat.getModifiedAt())
                    .isCommentDeleted(flat.getIsCommentDeleted())
                    .replies(new ArrayList<>())
                    .build();

            dtoMap.put(flat.getCommentId(), node);

            if (flat.getParentCommentId() == null) {
                result.add(node); // 최상위 댓글이면 리스트에 바로 추가
            } else {
                CommentTreeDto parent = dtoMap.get(flat.getParentCommentId());
                if (parent != null) {
                    parent.getReplies().add(node);
                }
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public MyCommentListResponse getMyComments(MyCommentSearchRequest searchRequest, int userId) {

        searchRequest.setUserId(userId);

        List<MyCommentResponseDto> originalComments = commentMapper.selectMyComments(searchRequest);

        long totalItems = commentMapper.countMyComments(searchRequest);

        List<MyCommentResponseDto> comments = new ArrayList<>();
        for (MyCommentResponseDto comment : originalComments) {
            if ("Y".equals(comment.getIsCommentDeleted())) {
                comments.add(comment.withContents("삭제된 댓글입니다."));
            } else if ("R".equals(comment.getIsCommentDeleted())) {
                comments.add(comment.withContents("신고된 댓글입니다."));
            } else {
                comments.add(comment); // 그대로 사용
            }
        }

        int page = searchRequest.getPage();
        int size = searchRequest.getSize();

        return MyCommentListResponse.builder()
                .comments(comments)
                .pagination(Pagination.builder()
                        .currentPage(page)
                        .totalPage((int) Math.ceil((double) totalItems / size))
                        .totalItems(totalItems)
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public MyCommentListResponse getMyreportedComments(MyCommentSearchRequest searchRequest, int userId) {
        searchRequest.setIsCommentDeleted("R"); // 신고된 댓글만 조회
        searchRequest.setUserId(userId);

        List<MyCommentResponseDto> comments = commentMapper.selectReportedComments(searchRequest);

        long totalItems = commentMapper.countReportedComments(searchRequest);

        int page = searchRequest.getPage();
        int size = searchRequest.getSize();

        return MyCommentListResponse.builder()
                .comments(comments)
                .pagination(Pagination.builder()
                        .currentPage(page)
                        .totalPage((int) Math.ceil((double) totalItems / size))
                        .totalItems(totalItems)
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public CommentDetailDto getReportedCommentById(int commentId) {
        return Optional.ofNullable(commentMapper.selectCommentById(commentId))
                .orElseThrow(() -> new NoSuchCommentException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
