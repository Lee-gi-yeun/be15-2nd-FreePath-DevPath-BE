package com.freepath.devpath.board.post.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freepath.devpath.board.post.command.dto.request.PostCreateRequest;
import com.freepath.devpath.board.post.command.dto.request.PostUpdateRequest;
import com.freepath.devpath.board.post.command.dto.response.PostCreateResponse;
import com.freepath.devpath.board.post.command.exception.FileUpdateFailedException;
import com.freepath.devpath.board.post.command.exception.InvalidPostAuthorException;
import com.freepath.devpath.board.post.command.exception.NoSuchPostException;
import com.freepath.devpath.board.post.command.service.PostCommandService;
import com.freepath.devpath.board.vote.command.dto.request.VoteCreateRequest;
import com.freepath.devpath.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PostCommandController.class)
class PostCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostCommandService postCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("게시글 업로드 테스트 (파일 없이)")
    void testUploadPostWithoutFiles() throws Exception {
        // Given
        VoteCreateRequest voteRequest = new VoteCreateRequest();
        ReflectionTestUtils.setField(voteRequest, "voteTitle", "좋아하는 언어는?");
        ReflectionTestUtils.setField(voteRequest, "voteDuedate", LocalDateTime.now().plusDays(3));
        ReflectionTestUtils.setField(voteRequest, "options", List.of("Java", "Python"));

        PostCreateRequest postCreateRequest = PostCreateRequest.builder()
                .boardCategory(1)
                .boardTitle("투표 포함 게시글")
                .boardContents("여러분의 의견을 듣고 싶어요!")
                .vote(voteRequest)
                .build();

        // Mock 파일 생성
        MockMultipartFile file1 = new MockMultipartFile(
                "files", // 파일 파라미터 이름 (컨트롤러의 @RequestPart 이름)
                "file1.txt", // 파일 이름
                "text/plain", // 파일의 MIME 타입
                "파일 내용".getBytes() // 파일 내용
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files", // 동일한 파라미터 이름을 사용해 여러 파일을 전송
                "file2.txt", // 파일 이름
                "text/plain", // 파일의 MIME 타입
                "두 번째 파일 내용".getBytes() // 파일 내용
        );

        // PostCreateResponse는 실제 구현에 맞게 설정
        PostCreateResponse postCreateResponse = new PostCreateResponse();

        when(postCommandService.createPost(any(PostCreateRequest.class), anyList(), anyInt()))
                .thenReturn(postCreateResponse);

        // JSON을 Multipart로 감싸서 보내기
        MockMultipartFile jsonPart = new MockMultipartFile(
                "postCreateRequest", // 컨트롤러의 @RequestPart 이름
                null,
                "application/json",
                objectMapper.writeValueAsBytes(postCreateRequest)
        );

        mockMvc.perform(multipart("/board")
                        .file(jsonPart)  // JSON 파트
                .file(new MockMultipartFile("files", new byte[0])))  // 빈 파일을 전달
                .andDo(print())// JWT 토큰 포함
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success").value(true));  // success가 true이어야 하는지 확인
    }


    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("게시글 업로드 테스트 (파일 포함)")
    void testUploadPostWithFiles() throws Exception {
        // Given
        VoteCreateRequest voteRequest = new VoteCreateRequest();
        ReflectionTestUtils.setField(voteRequest, "voteTitle", "좋아하는 언어는?");
        ReflectionTestUtils.setField(voteRequest, "voteDuedate", LocalDateTime.now().plusDays(3));
        ReflectionTestUtils.setField(voteRequest, "options", List.of("Java", "Python"));

        PostCreateRequest postCreateRequest = PostCreateRequest.builder()
                .boardCategory(1)
                .boardTitle("투표 포함 게시글")
                .boardContents("여러분의 의견을 듣고 싶어요!")
                .vote(voteRequest)
                .build();

        // Mock 파일 생성
        MockMultipartFile file1 = new MockMultipartFile(
                "files", // 파일 파라미터 이름 (컨트롤러의 @RequestPart 이름)
                "file1.txt", // 파일 이름
                "text/plain", // 파일의 MIME 타입
                "파일 내용".getBytes() // 파일 내용
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files", // 동일한 파라미터 이름을 사용해 여러 파일을 전송
                "file2.txt", // 파일 이름
                "text/plain", // 파일의 MIME 타입
                "두 번째 파일 내용".getBytes() // 파일 내용
        );

        // PostCreateResponse는 실제 구현에 맞게 설정
        PostCreateResponse postCreateResponse = new PostCreateResponse();

        when(postCommandService.createPost(any(PostCreateRequest.class), anyList(), anyInt()))
                .thenReturn(postCreateResponse);

        // JSON을 Multipart로 감싸서 보내기
        MockMultipartFile jsonPart = new MockMultipartFile(
                "postCreateRequest", // 컨트롤러의 @RequestPart 이름
                null,
                "application/json",
                objectMapper.writeValueAsBytes(postCreateRequest)
        );

        mockMvc.perform(multipart("/board")
                        .file(jsonPart)  // JSON 파트
                        .file(file1)     // 파일 파트 1
                        .file(file2))
                .andDo(print())// JWT 토큰 포함
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.success").value(true));  // success가 true이어야 하는지 확인
    }

    @DisplayName("게시글 수정 테스트")
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testUpdatePost() throws Exception {
        int boardId = 1;

        PostUpdateRequest postUpdateRequest = PostUpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용입니다.")
                .build();

        doNothing().when(postCommandService).updatePost(any(PostUpdateRequest.class), eq(boardId), anyInt());

        mockMvc.perform(put("/board/{board-id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postUpdateRequest)))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(status().isOk());
    }

    @DisplayName("게시글 삭제 테스트")
    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    void testDeletePost() throws Exception {
        int boardId = 1;

        doNothing().when(postCommandService).deletePost(eq(boardId), anyInt());

        mockMvc.perform(delete("/board/{board-id}", boardId))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글이 존재하지 않을 때 404 반환")
    @WithMockUser(username = "1", roles = {"USER"})
    void testUpdatePost_PostNotFound() throws Exception {
        int boardId = 999;
        PostUpdateRequest request = PostUpdateRequest.builder()
                .title("제목")
                .content("내용")
                .build();

        doThrow(new NoSuchPostException(ErrorCode.POST_NOT_FOUND))
                .when(postCommandService).updatePost(any(), eq(boardId), anyInt());

        mockMvc.perform(put("/board/{board-id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("20001"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글을 수정하려 할 때 403 반환")
    @WithMockUser(username = "2", roles = {"USER"}) // 작성자 아님
    void testUpdatePost_UnauthorizedUser() throws Exception {
        int boardId = 1;
        PostUpdateRequest request = PostUpdateRequest.builder()
                .title("수정 제목")
                .content("수정 내용")
                .build();

        doThrow(new InvalidPostAuthorException(ErrorCode.POST_UPDATE_FORBIDDEN))
                .when(postCommandService).updatePost(any(), eq(boardId), anyInt());

        mockMvc.perform(put("/board/{board-id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("20004"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 수정하려 할 때 410 반환")
    @WithMockUser(username = "1", roles = {"USER"})
    void testUpdatePost_AlreadyDeleted() throws Exception {
        int boardId = 1;
        PostUpdateRequest request = PostUpdateRequest.builder()
                .title("제목")
                .content("내용")
                .build();

        doThrow(new FileUpdateFailedException(ErrorCode.POST_ALREADY_DELETED))
                .when(postCommandService).updatePost(any(), eq(boardId), anyInt());

        mockMvc.perform(put("/board/{board-id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.errorCode").value("20008"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}

