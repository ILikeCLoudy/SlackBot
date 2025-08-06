package com.SKALA.LikeCloudy.Repository;

import com.SKALA.LikeCloudy.Entity.UserEntity;
import org.apache.commons.text.translate.NumericEntityUnescaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <UserEntity, Long> {
    Optional<UserEntity> findBySlackUserId(String slackUserId);
}
