package com.SKALA.LikeCloudy.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlackMessageService {

    @Value("${slack.bot-token}")
    private String botToken;

    @Value("${slack.postMessageUrl}")
    private String postMessageUrl;

    @Value("${slack.channel.monitor}")
    private String defaultChannel;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(botToken); // Authorization: Bearer xoxb-...

        Map<String, String> body = new HashMap<>();
        body.put("channel", defaultChannel);
        body.put("text", text);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(postMessageUrl, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Slack 메시지 전송 실패: " + response.getBody());
        }
    }
}
