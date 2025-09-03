package com.SKALA.LikeCloudy.Service;

import com.SKALA.LikeCloudy.DTO.VoteResultDTO;
import com.SKALA.LikeCloudy.DTO.VoteSummaryResponse;
import com.SKALA.LikeCloudy.Entity.MenuEntity;
import com.SKALA.LikeCloudy.Entity.VoteEntity;
import com.SKALA.LikeCloudy.Slack.SlackBlockBuilder;
import com.SKALA.LikeCloudy.Repository.MenuRepository;
import com.SKALA.LikeCloudy.Repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ResultService {
    private final MenuRepository menuRepository;
    private final VoteRepository voteRepository;
    private final SlackMessageService slackMessageService; // 기존 집계 메세지 아래 추가

    public VoteSummaryResponse getVoteSummary(LocalDate date) {
        // 오늘 날짜의 모든 메뉴를 조회합니다!
        List<MenuEntity> menus = menuRepository.findAllByMenuDate(date);

        List<VoteResultDTO> results = new ArrayList<>();

        for (MenuEntity menu : menus) {
            List<VoteEntity> votes = voteRepository.findByMenu(menu);
            List<String> voterNames = votes.stream()
                    .map(v -> v.getUser().getSlackUserName())
                    .collect(Collectors.toList());

            VoteResultDTO result = new VoteResultDTO (
                menu.getMenuType().name(),
                menu.getName(),
                votes.size(),
                voterNames
            );
            results.add(result);
        }
        return new VoteSummaryResponse(results, date);
    }
    // Slack Block Kit 구조로 가공합니다!
    public List<Map<String, Object>> formatSummaryForSlack(VoteSummaryResponse summary) {
        SlackBlockBuilder builder = new SlackBlockBuilder();
        builder.section(":fork_and_knife: *" + summary.getSurveyDate() + " 점심 투표 결과입니다.*");
        builder.divider();

        for (VoteResultDTO result : summary.getResults()) {
            String voterList = String.join(", ", result.getVoters());
            String text = String.format("• %s - %s (%d명): %s",
                    result.getMenuCode(), result.getMenuName(), result.getVoteCount(), voterList);
            builder.section(text);
        }
        return builder.build();
    }

    public void sendTodaySummaryToSlack() {
        VoteSummaryResponse summary = getVoteSummary(LocalDate.now());
        List<Map<String, Object>> blocks = formatSummaryForSlack(summary);
        slackMessageService.sendMessage(blocks);
    }
}
