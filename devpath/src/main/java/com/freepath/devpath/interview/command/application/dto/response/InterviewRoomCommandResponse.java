package com.freepath.devpath.interview.command.application.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class InterviewRoomCommandResponse {
    private Long interviewRoomId;
    private String interviewRoomTitle;
    private String interviewRoomStatus;
    private String difficultyLevel;
    private String evaluationStrictness;
    private String interviewRoomMemo;
    private String firstQuestion;
    private List<String> questions;
}
