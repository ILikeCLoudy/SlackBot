package com.SKALA.LikeCloudy.Service;

import com.SKALA.LikeCloudy.DTO.VoteResultDTO;
import com.SKALA.LikeCloudy.DTO.VoteSummaryResponse;
import com.SKALA.LikeCloudy.Entity.MenuEntity;
import com.SKALA.LikeCloudy.Entity.VoteEntity;
import com.SKALA.LikeCloudy.Repository.MenuRepository;
import com.SKALA.LikeCloudy.Repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    // Slack 메세지 형태로 가공합니다!
    public String formatSummaryForSlack(VoteSummaryResponse summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDF71 **").append(summary.getSurveyDate()).append(" 점심 투표 결과입니다. \n\n");

        for (VoteResultDTO result : summary.getResults()) {
            sb.append("• ").append(result.getMenuCode())
                    .append(" - ").append(result.getMenuName())
                    .append(" (").append(result.getVoteCount()).append("명): ");

            String voterList = String.join(", ", result.getVoters());
            sb.append(voterList).append("\n");
        }
        return sb.toString();
    }

    public void sendTodaySummaryToSlack() {
        VoteSummaryResponse summary = getVoteSummary(LocalDate.now());
        String message = formatSummaryForSlack((summary));
        slackMessageService.sendMessage(message);
    }
}
