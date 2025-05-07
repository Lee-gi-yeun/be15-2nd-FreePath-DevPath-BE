package com.freepath.devpath.chatting.command.domain.jpa.repository;

import com.freepath.devpath.chatting.command.domain.jpa.aggregate.ChattingRoom;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Integer> {
    @Query(value = """
    SELECT cj.chatting_room_id
    FROM chatting_join cj
    WHERE cj.user_id IN (:userId1, :userId2)
      AND cj.chatting_role = 'ONE'
    GROUP BY cj.chatting_room_id
    HAVING COUNT(DISTINCT cj.user_id) = 2
       AND SUM(CASE WHEN cj.chatting_join_status = 'Y' THEN 1 ELSE 0 END) >= 1
""", nativeQuery = true)
    Optional<Integer> findValidChattingRoom(@Param("userId1") int userId1, @Param("userId2") int userId2);


    Optional<ChattingRoom> findByBoardId(int boardId);
}
