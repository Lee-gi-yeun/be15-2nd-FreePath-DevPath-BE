package com.freepath.devpath.board.post.command.dto.request;

import com.freepath.devpath.board.vote.command.dto.request.VoteCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotNull(message = "게시글 카테고리는 필수입니다.")
    private Integer boardCategory;     // 게시글 카테고리 ID

    @NotBlank(message = "제목은 필수입니다.")
    private String boardTitle;         // 제목

    @NotBlank(message = "내용은 필수입니다.")
    private String boardContents;      // 내용

    private List<String> usedImageUrls;

    @Valid
    private VoteCreateRequest vote;
}
