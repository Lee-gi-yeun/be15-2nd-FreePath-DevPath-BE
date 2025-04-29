package com.freepath.devpath.board.vote.command.repository;

import com.freepath.devpath.board.vote.command.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Integer> {
    @Query(value = "SELECT b.user_id FROM vote v JOIN board b ON v.board_id = b.board_id WHERE v.vote_id = :voteId", nativeQuery = true)
    Integer findUserIdByVoteId(int voteId);

    Optional<Vote> findByBoardId(int boardId);
}
