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
            if (teamId != null && !teamId.isBlank() && !teamId.equals(user.getTeamId())) {
                // 세터가 없어서(현재) updateTeamId로 변경
                user.updateTeamId(teamId);
            }
            userRepository.save(user);
        }, () -> {
            UserEntity newbie = UserEntity.builder()
                            .slackUserId(slackUserId)
                            .teamId(teamId)
                            .slackUserName(null) // 이름은 slack/command 들어올때 갱신
                            .joinedAt(LocalDateTime.now())
                            .build();
            userRepository.save(newbie);
            log.info("[SlackEvent] user upsert: {}", slackUserId);
        });

        // (선택) 채널도 저장하고 싶으면 아래 주석 해제 + 엔티티/레포 추가
        // if (channelId != null && !channelId.isBlank()) {
        //     channelRepository.upsert(channelId, LocalDateTime.now());
        // }
    }

    @Transactional
    public void ensureUserName(String slackUserId, String slackUserName) {
        if (slackUserId == null || slackUserId.isBlank()) return;
        if (slackUserName == null || slackUserName.isBlank()) return;

        userRepository.findBySlackUserId(slackUserId).ifPresent(user -> {
            if (!slackUserName.equals(user.getSlackUserName())) {
                user.updateUserName(slackUserName);   // UserEntity에 이미 있음
                userRepository.save(user);
                log.info("[SlackEvent] user name updated: {} -> {}", user.getSlackUserId(), slackUserName);
            }
        });
    }
}
