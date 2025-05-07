package com.freepath.devpath.interview.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewAnswerCommandResponse {
    private Long interviewRoomId;
    private String userAnswer;
    private String gptEvaluation;
    private String nextQuestion;
    private Integer averageScore;
}