package com.SKALA.LikeCloudy.Service;

import com.SKALA.LikeCloudy.DTO.VoteRequestDTO;
import com.SKALA.LikeCloudy.Entity.MenuEntity;
import com.SKALA.LikeCloudy.Entity.UserEntity;
import com.SKALA.LikeCloudy.Entity.VoteEntity;
import com.SKALA.LikeCloudy.Repository.MenuRepository;
import com.SKALA.LikeCloudy.Repository.UserRepository;
import com.SKALA.LikeCloudy.Repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    public void vote(VoteRequestDTO request) {
        String slackUserId = request.getSlackUserId();
        String menuCode = request.getMenuCode();
        LocalDate today = LocalDate.now();

        // 이미 투표했는지 확인
        if (hasAlreadyVoted(slackUserId, today)) {
            throw new IllegalStateException("어... 투표 하셨는데요???");
        }

        // 사용자, 메뉴 조회
        UserEntity user = userRepository.findBySlackUserId(slackUserId)
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .slackUserId(slackUserId)
                            .slackUserName(request.getSlackUserName())
                            .teamId(request.getTeamId())
                            .joinedAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });
        MenuEntity.MenuType type = MenuEntity.MenuType.valueOf(menuCode);
        MenuEntity menu = menuRepository.findByMenuType(menuCode).orElseThrow(() -> new IllegalArgumentException("그런 메뉴는 읍써요..."));

        //VoteEntity 생성 및 저장용
        VoteEntity vote = VoteEntity.builder()
                .user(user)
                .menu(menu)
                .voteDate(today)
                .build();

        voteRepository.save(vote);
    }
    // 오늘 이미 사용자가 투표했다면?
    public boolean hasAlreadyVoted(String slackUserId, LocalDate date) {
        return voteRepository.existsByUser_SlackUserIdAndVoteDate(slackUserId, date);
    }
}
