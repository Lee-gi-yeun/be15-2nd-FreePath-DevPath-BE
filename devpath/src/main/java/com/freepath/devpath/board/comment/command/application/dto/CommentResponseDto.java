package com.freepath.devpath.board.comment.command.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private int commentId;
    private int userId;
    private Integer boardId;
    private Integer parentCommentId;
    private String contents;
    private Date createdAt;
    private Date modifiedAt;
    private Character deleted;
    private String nickname; // 👈 추가된 필드
}
