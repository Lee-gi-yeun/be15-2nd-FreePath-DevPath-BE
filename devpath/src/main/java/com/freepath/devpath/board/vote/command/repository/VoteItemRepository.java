package com.freepath.devpath.board.vote.command.repository;

import com.freepath.devpath.board.vote.command.entity.VoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteItemRepository extends JpaRepository<VoteItem, Integer> {
    List<VoteItem> findByVoteId(int voteId);
}
