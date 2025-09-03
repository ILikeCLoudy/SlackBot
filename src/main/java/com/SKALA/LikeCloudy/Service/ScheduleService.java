package com.SKALA.LikeCloudy.Service;

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

    /**
     * 오전 10시 30분에 오늘의 메뉴 안내 메시지를 전송합니다.
     */
    @Scheduled(cron = "0 30 10 * * *")
    public void sendMenuGuide() {
        String message = hystecMenuService.formatBundangBiwonLunchForSlack(
                hystecMenuService.fetchBundangBiwonLunch(LocalDate.now()));
        slackMessageService.sendMessage(message);
    }

    /**
     * 오전 11시 30분에 투표 결과를 전송합니다.
     */
    @Scheduled(cron = "0 30 11 * * *")
    public void sendVoteResult() {
        resultService.sendTodaySummaryToSlack();
    }
}

