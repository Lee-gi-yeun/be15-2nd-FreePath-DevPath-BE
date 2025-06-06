package com.freepath.devpath.board.comment.query.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Builder(toBuilder = true)
public class MyCommentResponseDto {

    private Integer boardId;
    private int commentId;
    private String boardTitle;
    private String contents;
    private Date createdAt;
    private Date modifiedAt;
    private String isCommentDeleted;
    private String nickName;

    public MyCommentResponseDto withContents(String newContents) {
        return this.toBuilder()
                .contents(newContents)
                .build();
    }
}
