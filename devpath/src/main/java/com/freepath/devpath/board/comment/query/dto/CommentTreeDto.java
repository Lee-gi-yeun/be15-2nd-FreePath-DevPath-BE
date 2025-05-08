package com.freepath.devpath.board.comment.query.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentTreeDto {
    private int userId;
    private int commentId;
    private String nickname;
    private String contents;
    private Date createdAt;
    private Date modifiedAt;
    private List<CommentTreeDto> replies; // 재귀적으로 트리 구조를 구성
}
