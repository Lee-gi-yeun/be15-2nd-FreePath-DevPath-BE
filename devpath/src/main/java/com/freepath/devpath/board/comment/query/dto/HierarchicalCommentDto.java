package com.freepath.devpath.board.comment.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class HierarchicalCommentDto {
    private int userId;
    private int commentId;
    private Integer parentCommentId; // null 가능
    private String nickname;
    private String contents;
    private Date createdAt;
    private Date modifiedAt;
    private int depth; // 계층 깊이
    private String isCommentDeleted;

}
