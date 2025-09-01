package com.SKALA.LikeCloudy.Slack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Component
public class SlackSignatureVerifier {

    @Value("${slack.signing-secret}")
    private String signingSecret;

    private static final long MAX_TOLERANCE_SECONDS = 60 * 5;

    public boolean verify(String requestTimestamp, String rawBody, String slackSignature, long nowEpochSeconds) {
        try {
            if (requestTimestamp == null || slackSignature == null) return false;

            long ts = Long.parseLong(requestTimestamp);
            if (Math.abs(nowEpochSeconds - ts) > MAX_TOLERANCE_SECONDS) {
                log.warn("[Slack] timestamp too old: now={}, ts={}", nowEpochSeconds, ts);
                return false;
            }

            // Mac 인스턴스는 매 호출마다 생성(스레드 안전)
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

            String base = "v0:" + requestTimestamp + ":" + rawBody;
            byte[] digest = mac.doFinal(base.getBytes(StandardCharsets.UTF_8));
            String mySig = "v0=" + bytesToHex(digest);

            // 타이밍 공격 방지 비교
            return MessageDigest.isEqual(
                    mySig.getBytes(StandardCharsets.UTF_8),
                    slackSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("[Slack] signature verify error", e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
