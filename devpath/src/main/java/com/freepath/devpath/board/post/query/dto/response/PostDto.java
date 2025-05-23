package com.freepath.devpath.board.post.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PostDto {
    private int boardId;
    private String boardTitle;
    private String nickname;
    private LocalDateTime boardCreatedAt;
}
