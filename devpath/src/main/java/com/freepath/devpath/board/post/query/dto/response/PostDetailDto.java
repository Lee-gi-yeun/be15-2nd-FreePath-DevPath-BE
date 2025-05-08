package com.freepath.devpath.board.post.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostDetailDto {
    private int userId;
    private String nickname;
    private String boardTitle;
    private String boardContents;
    private Date boardCreatedAt;
    private Date boardModifiedAt;
    private String isBoardDeleted;
    private List<String> s3Urls;
}
