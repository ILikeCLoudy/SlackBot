package com.SKALA.LikeCloudy.Service;

import com.SKALA.LikeCloudy.DTO.VoteSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final SlackMessageService slackMessageService;
    private final HystecMenuService hystecMenuService;
    private final ResultService resultService;

    @Scheduled(cron = "0 30 10 * * *")
    public void sendMenuGuideMessage() {
        String menuMessage = hystecMenuService.formatBundangBiwonLunchForSlack(
                hystecMenuService.fetchBundangBiwonLunch(LocalDate.now()));
        slackMessageService.sendMessage(menuMessage);
    }

    @Scheduled(cron = "0 30 11 * * *")
    public void sendVoteResultMessage() {
        VoteSummaryResponse summary = resultService.getVoteSummary(LocalDate.now());
        String message = resultService.formatSummaryForSlack(summary);
        slackMessageService.sendMessage(message);
    }
}
