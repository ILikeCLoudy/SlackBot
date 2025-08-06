package com.SKALA.LikeCloudy.Service;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SlackService {

    private final Slack slack = Slack.getInstance();

    @Value("${slack.bot-token}")
    private String botToken;

    @Value("${slack.channel.monitor}")
    private String channel;

    public void sendMessage(String text) {
        try {
            ChatPostMessageResponse response = slack.methods(botToken).chatPostMessage(req -> req
                    .channel(channel)
                    .text(text)
            );

            if (!response.isOk()) {
                System.out.println("Slack 전송 실패: " + response.getError());
            } else {
                System.out.println("Slack 전송 성공");
            }

        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }
}

