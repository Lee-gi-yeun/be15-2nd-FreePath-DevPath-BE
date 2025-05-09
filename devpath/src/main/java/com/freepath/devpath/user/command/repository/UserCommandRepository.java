package com.freepath.devpath.user.command.repository;

import com.freepath.devpath.user.command.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCommandRepository extends JpaRepository<User, Long> {

    List<User> findByItNewsSubscription(String y);

    Optional<User> findByUserIdAndUserDeletedAtIsNull(Integer userId);

    Optional<User> findByEmailAndUserDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsUserRestricted(String email, String isUserRestricted);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email AND u.userDeletedAt IS NULL")
    int updatePassword(String email, String password);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.email = :newEmail WHERE u.email = :email AND u.userDeletedAt IS NULL")
    int updateEmail(String email, String newEmail);

    Optional<User> findByEmailAndLoginIdAndUserDeletedAtIsNull(String email, String loginId);

    boolean existsByNicknameAndUserDeletedAtIsNull(String nickname);

    Optional<User> findByLoginIdAndLoginMethodAndUserDeletedAtIsNull(String email, String loginMethod);

    boolean existsByEmailAndUserDeletedAtIsNull(String email);

    boolean existsByLoginIdAndLoginMethodAndIsUserRestricted(String email, String google, String y);

    List<User> findByUserIdIn(Collection<Integer> userIds);

    boolean existsByLoginIdAndUserDeletedAtIsNull(String loginId);

    Optional<User> findByLoginIdAndUserDeletedAtIsNull(String loginId);

    @Query("SELECT u.nickname FROM User u WHERE u.userId = :userId AND u.userDeletedAt IS NULL")
    Optional<String> findNicknameByUserId(Integer userId);
}