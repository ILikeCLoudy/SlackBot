package com.SKALA.LikeCloudy.Service;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {
    @Value("${SlackbotTokens}")
    private String Slackbot;
}
