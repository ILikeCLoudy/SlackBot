package com.SKALA.LikeCloudy.Service;

import com.SKALA.LikeCloudy.Entity.UserEntity;
import com.SKALA.LikeCloudy.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackEventService {

    private final UserRepository userRepository;
    // (선택) private final ChannelRepository channelRepository;  // 채널 저장 원하면 추가

    /**
     * 채널/팀 합류 시 사용자 upsert
     */
    @Transactional
    public void upsertUserOnJoin(String teamId, String slackUserId, String channelId) {
        if (slackUserId == null || slackUserId.isBlank()) return;

        userRepository.findBySlackUserId(slackUserId).ifPresentOrElse(user -> {
            // 팀 정보만 최신화 (이름은 Slash Command로도 들어오므로 거기에서 갱신)
            if (teamId != null && !teamId.isBlank()) user.setTeamId(teamId);
            userRepository.save(user);
        }, () -> {
            UserEntity newbie = new UserEntity();
            newbie.setSlackUserId(slackUserId);
            newbie.setTeamId(teamId);
            newbie.setSlackUserName(null); // 이름은 미상 → /slack/command 들어올 때 업데이트됨
            newbie.setJoinedAt(LocalDateTime.now());
            userRepository.save(newbie);
            log.info("[SlackEvent] user upsert: {}", slackUserId);
        });

        // (선택) 채널도 저장하고 싶으면 아래 주석 해제 + 엔티티/레포 추가
        // if (channelId != null && !channelId.isBlank()) {
        //     channelRepository.upsert(channelId, LocalDateTime.now());
        // }
    }
}
