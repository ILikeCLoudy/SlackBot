package com.SKALA.LikeCloudy.Controller;

import com.SKALA.LikeCloudy.Service.SlackEventService;
import com.SKALA.LikeCloudy.Slack.SlackSignatureVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class SlackEventController {

    private final SlackSignatureVerifier signatureVerifier;
    private final SlackEventService slackEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(
            value = "/events",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=UTF-8"
    )
    public ResponseEntity<?> handleEvents(
            @RequestHeader(name = "X-Slack-Signature", required = false) String xSlackSignature,
            @RequestHeader(name = "X-Slack-Request-Timestamp", required = false) String xSlackTimestamp,
            @RequestHeader(name = "X-Slack-Retry-Num", required = false) String xSlackRetryNum,
            @RequestHeader(name = "X-Slack-Retry-Reason", required = false) String xSlackRetryReason,
            @RequestBody String rawBody
    ) {
        // 0) 서명 검증 (5분 윈도우)
        long now = Instant.now().getEpochSecond();
        if (!signatureVerifier.verify(xSlackTimestamp, rawBody, xSlackSignature, now)) {
            log.warn("[SlackEvents] invalid signature");
            // Slack은 4xx/5xx 시 재시도 → 보통 401로 명확히 거절해도 OK
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "invalid_signature"));
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String type = root.path("type").asText("");

            // 1) URL Verification
            if ("url_verification".equals(type)) {
                String challenge = root.path("challenge").asText("");
                return ResponseEntity.ok(Map.of("challenge", challenge));
            }

            // 2) 재시도 헤더 로깅 (멱등 고려)
            if (StringUtils.hasText(xSlackRetryNum)) {
                log.info("[Slack] retry delivery: num={}, reason={}", xSlackRetryNum, xSlackRetryReason);
                // 일반적으로 동일 처리 후 200 반환
            }

            // 3) 일반 이벤트 처리
            if ("event_callback".equals(type)) {
                String teamId = root.path("team_id").asText("");
                JsonNode event = root.path("event");
                String eventType = event.path("type").asText("");

                switch (eventType) {
                    case "member_joined_channel" -> {
                        String userId   = extractId(event.path("user"));
                        String channelId= extractId(event.path("channel"));
                        if (StringUtils.hasText(userId)) {
                            slackEventService.upsertUserOnJoin(teamId, userId, channelId);
                        }
                    }
                    case "team_join" -> {
                        String userId = event.path("user").path("id").asText("");
                        if (StringUtils.hasText(userId)) {
                            slackEventService.upsertUserOnJoin(teamId, userId, null);
                        }
                    }
                    case "reaction_added", "reaction_removed" -> {
                        // (선택) 이후 이모지 투표용 훅
                    }
                    default -> log.debug("[Slack] ignored event type: {}", eventType);
                }
            }

            // Slack은 빠른 2xx 응답을 선호
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("[SlackEvents] error", e);
            // 재시도 폭주 방지: 멱등 설계가 되어있다면 200으로 흡수하는 편이 안전
            return ResponseEntity.ok(Map.of("ok", true));
        }
    }

    private static String extractId(JsonNode node) {
        if (node.isTextual()) return node.asText();
        if (node.isObject())  return node.path("id").asText("");
        return "";
    }
}
