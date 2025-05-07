package com.freepath.devpath.interview.query.mapper;

import com.freepath.devpath.interview.query.dto.InterviewDetailDto;
import com.freepath.devpath.interview.query.dto.InterviewRoomDetailResponse;
import com.freepath.devpath.interview.query.dto.InterviewRoomDto;
import com.freepath.devpath.interview.query.dto.ReexecutedRoomDto;
import com.freepath.devpath.interview.query.service.InterviewQueryAdminService;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InterviewMapper {

    /* InterviewQueryService */
    /* 사용자가 진행한 면접방 목록 조회 */
    List<InterviewRoomDto> selectInterviewRoomListByUserIdExcludingExpired(Long userId, int size, int offset);
    int countInterviewRoomListByUserIdExcludingExpired(Long userId);

    /* 사용자가 면접방 목록 조회 시 필터 적용 */
    List<InterviewRoomDto> selectInterviewRoomListByFilter(
            Long userId,
            String category,
            String difficultyLevel,
            String evaluationStrictness,
            String sortOrder, // 추가된 파라미터
            int size,
            int offset
    );

    int countInterviewRoomListByFilter(
            Long userId,
            String category,
            String difficultyLevel,
            String evaluationStrictness
    );

    /* 면접방에 대한 정보 조회 */
    InterviewRoomDetailResponse selectInterviewRoomByRoomId(Long interviewRoomId);

    /* 면접방에 있는 면접 내역 조회 */
    List<InterviewDetailDto> selectInterviewListByRoomId(Long interviewRoomId);

    /* 재실행된 면접방 목록 조회 */
    List<ReexecutedRoomDto> selectReexecutedRoomsByParentId(Long parentInterviewRoomId);

    /* 면접방의 총평 조회 */
    String selectInterviewSummaryByRoomId(Long roomId);


    /* InterviewQueryAdminService */
    /* 모든 유저에 대한 면접방 목록 조회 */
    List<InterviewRoomDto> selectAllInterviewRooms(int page, int size, int offset);
    int countAllInterviewRooms();

    /* 전체 유저 면접방 목록 조회에 필터 적용 */
    List<InterviewRoomDto> selectAdminInterviewRoomListByFilter(String status, String category, String difficultyLevel, String evaluationStrictness, String sortOrder, int size, int offset);
    int countAdminInterviewRoomListByFilter( String status, String category, String difficultyLevel, String evaluationStrictness );


}