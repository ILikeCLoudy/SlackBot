package com.SKALA.LikeCloudy.Controller;

import com.SKALA.LikeCloudy.Service.SlackEventService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class SlackEventController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SlackEventService slackEventService;

    @Value("${slack.signing-secret}")
    private String signingSecret;

    @PostMapping(value = "/events", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> handleEvents(@RequestBody String body, HttpServletRequest request) {
        // 0) Slack 서명 검증 (5분 윈도우)
        if (!verifySlackSignature(request, body)) {
            log.warn("[SlackEvents] signature verify failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"ok\":false}");
        }

        try {
            // 1) url_verification 핸드쉐이크
            JsonNode root = objectMapper.readTree(body);
            String type = root.path("type").asText("");
            if ("url_verification".equals(type)) {
                String challenge = root.path("challenge").asText("");
                return ResponseEntity.ok("{\"challenge\":\"" + challenge + "\"}");
            }

            // Slack 재시도 이벤트는 무시(중복 처리 방지)
            if (StringUtils.hasText(request.getHeader("X-Slack-Retry-Num"))) {
                return ResponseEntity.ok("{\"ok\":true}");
            }

            // 2) 일반 이벤트
            if ("event_callback".equals(type)) {
                String teamId = root.path("team_id").asText(""); // v2 이벤트 엔벨로프
                JsonNode event = root.path("event");
                String eventType = event.path("type").asText("");

                switch (eventType) {
                    case "member_joined_channel": {
                        String userId = extractId(event.path("user"));
                        String channelId = extractId(event.path("channel"));
                        slackEventService.upsertUserOnJoin(teamId, userId, channelId);
                        break;
                    }
                    case "team_join": { // 워크스페이스에 유저가 새로 들어온 경우
                        String userId = event.path("user").path("id").asText("");
                        slackEventService.upsertUserOnJoin(teamId, userId, null);
                        break;
                    }
                    case "reaction_added":
                    case "reaction_removed":
                        // (선택) 이모지 투표 처리 지점. 이후 단계에서 구현.
                        break;
                    default:
                        // 필요시 확장
                        break;
                }
            }

            return ResponseEntity.ok("{\"ok\":true}");
        } catch (Exception e) {
            log.error("[SlackEvents] error", e);
            return ResponseEntity.status(500).body("{\"ok\":false}");
        }
    }

    private boolean verifySlackSignature(HttpServletRequest request, String body) {
        try {
            String timestamp = request.getHeader("X-Slack-Request-Timestamp");
            String signature = request.getHeader("X-Slack-Signature"); // v0=xxxx

            if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(signature)) return false;

            long ts = Long.parseLong(timestamp);
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - ts) > 60 * 5) return false; // 5분 이내만 허용

            String base = "v0:" + timestamp + ":" + body;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(base.getBytes(StandardCharsets.UTF_8));
            String mySig = "v0=" + bytesToHex(digest);

            // 타이밍 공격 방지 비교
            return MessageDigest.isEqual(mySig.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("verifySlackSignature error", e);
            return false;
        }
    }

    private static String extractId(JsonNode node) {
        if (node.isTextual()) return node.asText();
        if (node.isObject()) return node.path("id").asText("");
        return "";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
