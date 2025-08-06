package com.SKALA.LikeCloudy.Repository;

import com.SKALA.LikeCloudy.Entity.MenuEntity;
import com.SKALA.LikeCloudy.Entity.UserEntity;
import com.SKALA.LikeCloudy.Entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository <VoteEntity, Long> {
    List<VoteEntity> findByMenu(MenuEntity menu); // 특정 메뉴에 대한 투표내역
    List<VoteEntity> findByUser(UserEntity user); // 특정 유저의 투표 내역
    boolean existsByUser_SlackUserIdAndVoteDate(String slackUserId, LocalDate voteDate);

    Optional<VoteEntity> findByUserAndMenu(UserEntity user, MenuEntity menu);
}
