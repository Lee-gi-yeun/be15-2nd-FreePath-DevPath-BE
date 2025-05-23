package com.freepath.devpath.chatting.command.domain.jpa.repository;

import com.freepath.devpath.chatting.command.domain.jpa.aggregate.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock,Integer> {
    Optional<UserBlock> findByBlockerIdAndBlockedId(int blockerId, int blockedId);

    int deleteUserBlockByBlockerIdAndBlockedId(int blockerId, int blockedId);

}
