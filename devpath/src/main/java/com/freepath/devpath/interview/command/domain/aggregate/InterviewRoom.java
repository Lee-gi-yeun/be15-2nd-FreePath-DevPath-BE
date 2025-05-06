package com.freepath.devpath.interview.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InterviewRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewRoomId;

    private Long userId;

    private Long parentInterviewRoomId;

    private String interviewCategory;

    @Column(nullable = false)
    private String interviewRoomTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationStrictness evaluationStrictness;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewRoomStatus interviewRoomStatus;

    @Column
    private String interviewRoomMemo;

    private LocalDateTime interviewRoomCreatedAt;

    @Column
    private Integer averageScore;

    @PrePersist
    protected void onCreate() {
        this.interviewRoomCreatedAt = LocalDateTime.now();
        this.interviewRoomStatus = InterviewRoomStatus.PROGRESS;
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.MEDIUM;
        this.evaluationStrictness = evaluationStrictness != null ? evaluationStrictness : EvaluationStrictness.NORMAL;
    }

    /* 면접방 제목을 변경 */
    public void updateTitle(String title) {
        this.interviewRoomTitle = title;
    }

    /* 면접방 상태를 변경 */
    public void updateStatus(InterviewRoomStatus status) {
        this.interviewRoomStatus = status;
    }

    /* 면접방 메모를 변경 */
    public void updateMemo(String memo) {
        this.interviewRoomMemo = memo;
    }

    /* 면접방 평점을 등록 */
    public void updateAverageScore(int averageScore) {
        this.averageScore = averageScore;
    }


}