package com.freepath.devpath.interview.command.application.service;

import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.interview.command.application.dto.request.InterviewAnswerCommandRequest;
import com.freepath.devpath.interview.command.application.dto.request.InterviewRoomUpdateCommandRequest;
import com.freepath.devpath.interview.command.application.dto.response.InterviewAnswerCommandResponse;
import com.freepath.devpath.interview.command.application.dto.response.InterviewRoomCommandResponse;
import com.freepath.devpath.interview.command.domain.aggregate.*;
import com.freepath.devpath.interview.command.domain.repository.InterviewRepository;
import com.freepath.devpath.interview.command.domain.repository.InterviewRoomRepository;
import com.freepath.devpath.interview.command.exception.*;
import com.freepath.devpath.interview.command.infrastructure.gpt.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterviewCommandService {

    private final InterviewRoomRepository interviewRoomRepository;
    private final InterviewRepository interviewRepository;
    private final GptService gptService;

    /* 카테고리 선택으로 면접방 생성하고 첫 질문 도출*/
    @Transactional
    public InterviewRoomCommandResponse createRoomAndFirstQuestion(
            Long userId,
            String category,
            DifficultyLevel difficultyLevel,
            EvaluationStrictness evaluationStrictness
    ) {

        // 0. 면접방 기본 정보 생성
        difficultyLevel = Optional.ofNullable(difficultyLevel)
                .orElse(DifficultyLevel.MEDIUM);
        evaluationStrictness = Optional.ofNullable(evaluationStrictness)
                .orElse(EvaluationStrictness.NORMAL);
        String title = category + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));

        // 1. 면접방 생성 및 저장
        InterviewRoom room = null;
         try{
            room = interviewRoomRepository.save(
                    InterviewRoom.builder()
                            .userId(userId)
                            .interviewCategory(category)
                            .interviewRoomTitle(title)
                            .difficultyLevel(difficultyLevel)
                            .evaluationStrictness(evaluationStrictness)
                            .build()
            );
         } catch(Exception e){
             throw new InterviewRoomCreationException(ErrorCode.INTERVIEW_ROOM_CREATION_FAILED);
         }


        // 2. GPT로부터 첫 질문 생성
        String firstQuestion = gptService.generateQuestion(category, difficultyLevel);

        // 3. 질문 내용을 INTERVIEW 테이블에 저장
        interviewRepository.save(
                Interview.builder()
                        .interviewRoomId(room.getInterviewRoomId())
                        .interviewRole(InterviewRole.AI)
                        .interviewMessage(firstQuestion)
                        .build()
        );

        // 4. 응답
        return InterviewRoomCommandResponse.builder()
                .interviewRoomId(room.getInterviewRoomId())
                .interviewRoomTitle(room.getInterviewRoomTitle())
                .interviewRoomStatus(room.getInterviewRoomStatus().name())
                .difficultyLevel(room.getDifficultyLevel().name())
                .evaluationStrictness(room.getEvaluationStrictness().name())
                .interviewRoomMemo(room.getInterviewRoomMemo())
                .firstQuestion(firstQuestion)
                .build();
    }

    /* 질문에 대한 답변, 답변에 대한 평가, 다음 질문 도출 */
    @Transactional
    public InterviewAnswerCommandResponse answerAndEvaluate(
            Long userId, Long roomId,
            InterviewAnswerCommandRequest request
    ) {

        // 0-1. 면접방 존재 여부 확인
        InterviewRoom room = interviewRoomRepository.findById(roomId)
                .orElseThrow(() -> new InterviewRoomNotFoundException(ErrorCode.INTERVIEW_ROOM_NOT_FOUND));

        // 0-2. 면접방 진행자 검증
        if(!room.getUserId().equals(userId)){
            throw new InterviewRoomAccessException(ErrorCode.INTERVIEW_ROOM_ACCESS_DENIED);
        }

        // 0-3. 면접방 당 면접 개수 검증
        int interviewIndex = request.getInterviewIndex();
        if(interviewIndex<1 || interviewIndex>3){
            throw new InterviewIndexInvalidException(ErrorCode.INTERVIEW_INDEX_INVALID);
        }

        // 0-4. 면접 답변 내용 검증
        if(request.getUserAnswer() == null || request.getUserAnswer().isEmpty()){
            throw new InterviewAnswerEmptyException(ErrorCode.INTERVIEW_ANSWER_EMPTY);
        }

        // 1. 사용자의 답변 저장
        interviewRepository.save(
                Interview.builder()
                        .interviewRoomId(roomId)
                        .interviewRole(InterviewRole.USER)
                        .interviewMessage(request.getUserAnswer())
                        .build()
        );

        // 2. GPT 평가 생성
        Interview question
                = interviewRepository.findTopByInterviewRoomIdAndInterviewRoleOrderByInterviewIdDesc(
                            roomId,InterviewRole.AI)
                    .orElseThrow(
                            () -> new InterviewEvaluationCreationException(ErrorCode.INTERVIEW_EVALUATION_FAILED)
                    );

        EvaluationStrictness evaluationStrictness = Optional.ofNullable(room.getEvaluationStrictness())
                .orElse(EvaluationStrictness.NORMAL);
        String evaluation = gptService.evaluateAnswer(
                question.getInterviewMessage(),
                request.getUserAnswer(),
                evaluationStrictness
        );

        // 3. GPT 평가 저장
        interviewRepository.save(
                Interview.builder()
                        .interviewRoomId(roomId)
                        .interviewRole(InterviewRole.AI)
                        .interviewMessage(evaluation)
                        .build()
        );

        // 4. 다음 질문 생성 (면접방 당 3회 면접 실행)
        String nextQuestion = null;
        DifficultyLevel difficultyLevel = Optional.ofNullable(room.getDifficultyLevel())
                .orElse(DifficultyLevel.MEDIUM);
        if (interviewIndex < 3) {
            if (room.getParentInterviewRoomId() != null) {
                // 재실행 면접방인 경우, 기존 질문 복사본을 순서대로 사용한다
                List<Interview> copiedQuestions = interviewRepository.findByInterviewRoomIdAndInterviewRoleOrderByInterviewIdAsc(roomId, InterviewRole.AI);
                nextQuestion = copiedQuestions.get(interviewIndex).getInterviewMessage(); // index 1이면 두 번째 질문
            } else {
                // 재실행 면접방이 아니라 새로운 면접방인 경우
                List<String> previousQuestions = interviewRepository.findByInterviewRoomId(roomId).stream()
                        .filter(i -> i.getInterviewRole() == InterviewRole.AI)
                        .map(Interview::getInterviewMessage)
                        .filter(msg -> msg.startsWith("[면접 질문]"))
                        .toList();

                nextQuestion = gptService.generateQuestion(
                        room.getInterviewCategory(), difficultyLevel,
                        previousQuestions
                );
            }

            interviewRepository.save(
                    Interview.builder()
                            .interviewRoomId(roomId)
                            .interviewRole(InterviewRole.AI)
                            .interviewMessage(nextQuestion)
                            .build()
            );
        }

        // 5. 평균 점수 계산
        List<String> evaluationMessages = interviewRepository.findByInterviewRoomId(roomId).stream()
                .filter(i -> i.getInterviewRole() == InterviewRole.AI)
                .map(Interview::getInterviewMessage)
                .filter(msg -> msg.startsWith("[답변 평가]"))
                .toList();

        List<Integer> scores = evaluationMessages.stream()
                .map(this::extractScore)
                .filter(Objects::nonNull)
                .toList();

        int averageScore = scores.isEmpty() ? 0 :
                (int) scores.stream().mapToInt(Integer::intValue).average().orElse(0);

        // 6. 마지막 답변이라면 면접 총평 생성하고 면접방 상태 변경
        if(interviewIndex == 3){
            List<String> gptEvaluations = interviewRepository.findByInterviewRoomId(roomId).stream()
                    .filter(interview -> interview.getInterviewRole() == InterviewRole.AI)
                    .map(Interview::getInterviewMessage)
                    .filter(msg -> msg.startsWith("[답변 평가]"))
                    .toList();
            String summary = gptService.summarizeInterview(gptEvaluations, evaluationStrictness, difficultyLevel);

            interviewRepository.save(
                    Interview.builder()
                            .interviewRoomId(roomId)
                            .interviewRole(InterviewRole.AI)
                            .interviewMessage("[총평]"+summary)
                            .build()
            );

            room.updateStatus(InterviewRoomStatus.COMPLETED);

            room.updateAverageScore(averageScore);
        }

        // 7. 응답
        return InterviewAnswerCommandResponse.builder()
                .interviewRoomId(roomId)
                .userAnswer(request.getUserAnswer())
                .gptEvaluation(evaluation)
                .nextQuestion(nextQuestion)
                .averageScore(averageScore)
                .build();
    }

    private Integer extractScore(String evaluation) {
        try {
            int startIdx = evaluation.indexOf("총점: #") + 5;
            if (startIdx >= 5) {
                int endIdx = evaluation.indexOf("\n", startIdx);
                String scoreStr = (endIdx != -1)
                        ? evaluation.substring(startIdx, endIdx).trim()
                        : evaluation.substring(startIdx).trim();
                return Integer.parseInt(scoreStr);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /* 기존의 면접방 재실행 */
    @Transactional
    public InterviewRoomCommandResponse reexecuteInterviewRoom(
            Long userId, Long originalRoomId,
            EvaluationStrictness newStrictness
    ) {
        // 1. 기존 면접방 조회
        InterviewRoom originalRoom = interviewRoomRepository.findById(originalRoomId)
                .orElseThrow(() -> new InterviewRoomNotFoundException(ErrorCode.INTERVIEW_ROOM_NOT_FOUND));

        // 2. 기존 면접 질문만 추출 (AI 질문만, 총평 제외)
        List<Interview> originalQuestions = interviewRepository.findByInterviewRoomId(originalRoomId).stream()
                .filter(i -> i.getInterviewRole() == InterviewRole.AI)
                .filter(i -> i.getInterviewMessage() != null && i.getInterviewMessage().startsWith("[면접 질문]"))
                .toList();

        if (originalQuestions.size() != 3) {
            throw new InterviewQuestionCreationException(ErrorCode.INTERVIEW_QUESTION_CREATION_FAILED);
        }

        // 3. 새로운 면접방 제목 생성
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        String newTitle = originalRoom.getInterviewRoomTitle() + "_" + suffix;

        // 4. 새 면접방 생성
        InterviewRoom newRoom = interviewRoomRepository.save(
                InterviewRoom.builder()
                        .userId(userId)
                        .interviewCategory(originalRoom.getInterviewCategory())
                        .interviewRoomTitle(newTitle)
                        .difficultyLevel(originalRoom.getDifficultyLevel())
                        .evaluationStrictness(Optional.ofNullable(newStrictness).orElse(EvaluationStrictness.NORMAL))
                        .parentInterviewRoomId(originalRoomId)
                        .build()
        );

        // 3. 새 방 ID로 3개 질문 저장
        List<String> questionMessages = new ArrayList<>();
        for (Interview q : originalQuestions) {
            String msg = q.getInterviewMessage();
            questionMessages.add(msg);
            interviewRepository.save(Interview.builder()
                    .interviewRoomId(newRoom.getInterviewRoomId())
                    .interviewRole(InterviewRole.AI)
                    .interviewMessage(msg)
                    .build()
            );
        }

        // 6. 응답 반환
        return InterviewRoomCommandResponse.builder()
                .interviewRoomId(newRoom.getInterviewRoomId())
                .interviewRoomTitle(newRoom.getInterviewRoomTitle())
                .difficultyLevel(newRoom.getDifficultyLevel().name())
                .evaluationStrictness(newRoom.getEvaluationStrictness().name())
                .questions(questionMessages)
                .build();
    }


    /* 면접방 삭제 */
    @Transactional
    public void deleteInterviewRoom(Long userId, Long roomId) {

        // 면접방 존재 여부 확인
        InterviewRoom room = interviewRoomRepository.findById(roomId)
                .orElseThrow(() -> new InterviewRoomNotFoundException(ErrorCode.INTERVIEW_ROOM_NOT_FOUND));

        // 소유자 검증
        if (!room.getUserId().equals(userId)) {
            throw new InterviewRoomAccessException(ErrorCode.INTERVIEW_ROOM_ACCESS_DENIED);
        }

        try {
            // 1. 면접 이력 삭제
            interviewRepository.deleteByInterviewRoomId(roomId);

            // 2. 면접방 삭제
            interviewRoomRepository.deleteById(roomId);
        } catch (Exception e) {
            throw new InterviewRoomDeleteException(ErrorCode.INTERVIEW_ROOM_DELETE_FAILED);
        }
    }

    /* 면접방 정보 수정 */
    @Transactional
    public void updateInterviewRoom(
            Long userId, Long roomId,
            InterviewRoomUpdateCommandRequest request
    ) {

        // 면접방 존재 여부 확인
        InterviewRoom room = interviewRoomRepository.findById(roomId)
                .orElseThrow(() -> new InterviewRoomNotFoundException(ErrorCode.INTERVIEW_ROOM_NOT_FOUND));

        // 면접방 진행자 검증
        if (!room.getUserId().equals(userId)) {
            throw new InterviewRoomAccessException(ErrorCode.INTERVIEW_ROOM_ACCESS_DENIED);
        }

        // 면접방 제목 수정
        if (request.getInterviewRoomTitle() == null || request.getInterviewRoomTitle().isEmpty() || request.getInterviewRoomTitle().isBlank() || request.getInterviewRoomTitle().trim().isEmpty()) {
            throw new InterviewRoomTitleInvalidException(ErrorCode.INTERVIEW_ROOM_TITLE_INVALID);
        }
        room.updateTitle(request.getInterviewRoomTitle());

        // 면접방 메모 수정
        room.updateMemo(request.getInterviewRoomMemo());
    }

}
