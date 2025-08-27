package com.SKALA.LikeCloudy.Service;

import com.SKALA.LikeCloudy.DTO.hystec.HystecMenuItem;
import com.SKALA.LikeCloudy.DTO.hystec.HystecMenuResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class HystecMenuService {

    private final WebClient hystecWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PATH = "/V3/prc/selectMenuList.prc";
    private static final String CAMPUS_BUNDANG = "BD";
    private static final String CAFETERIA_BIWON = "21"; // 비원
    private static final String MEAL_LUNCH = "LN";      // 점심
    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    /** 분당-비원 오늘 '점심' 메뉴 원본 응답 */
    public HystecMenuResponse fetchBundangBiwonLunch(LocalDate date) {
        String ymd = date.format(YMD);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("campus",       CAMPUS_BUNDANG);   // BD
        form.add("cafeteriaSeq", CAFETERIA_BIWON);  // 21
        form.add("mealType",     MEAL_LUNCH);       // LN
        form.add("ymd",          ymd);              // yyyyMMdd

        String raw = hystecWebClient.post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class) // 서버가 text/html로 내려주므로 문자열로 받는다
                .block();

        try {
            return objectMapper.readValue(Objects.requireNonNull(raw), HystecMenuResponse.class);
        } catch (Exception e) {
            log.error("Hystec 응답 파싱 실패. raw={}", raw, e);
            throw new IllegalStateException("Hystec 메뉴 응답 파싱 실패", e);
        }
    }

    /** 슬랙 전송용 한 줄 요약 텍스트 생성 */
    public String formatBundangBiwonLunchForSlack(HystecMenuResponse res) {
        if (res == null || res.menuList() == null || res.menuList().isEmpty()) {
            return "*[분당 비원] 점심* — 데이터 없음";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("*[분당 비원] 점심*");
        if (res.temperature() != null && !res.temperature().isBlank()) {
            sb.append(" · ").append(res.temperature()).append("℃");
        }
        sb.append("\n");

        for (HystecMenuItem m : res.menuList()) {
            String courseShort = m.courseName()
                    .replace("프리미엄건강식", "프리미엄")
                    .replace("코너", "");

            String kcal = (m.kcal() == null || m.kcal().isBlank()) ? "" : " (" + m.kcal() + "kcal)";
            String star = (m.avgStar() != null && !m.avgStar().isBlank())
                    ? "  ⭐" + m.avgStar() + "(" + (m.satiCnt() == null ? "0" : m.satiCnt()) + ")"
                    : "";

            List<String> sides = Stream.of(m.side1(), m.side2(), m.side3(), m.side4(), m.side5(), m.side6())
                    .filter(s -> s != null && !s.isBlank())
                    .toList();

            sb.append(courseShort).append(") ")
                    .append(m.menuName()).append(kcal)
                    .append(" - ").append(String.join(", ", sides))
                    .append(star).append("\n");
        }
        return sb.toString().trim();
    }
}
