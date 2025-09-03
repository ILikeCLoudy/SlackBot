package com.SKALA.LikeCloudy.Controller;
import com.SKALA.LikeCloudy.DTO.MenuDTO;
import com.SKALA.LikeCloudy.DTO.VoteRequestDTO;
import com.SKALA.LikeCloudy.DTO.VoteSummaryResponse;
import com.SKALA.LikeCloudy.Service.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slack")
public class SlackController {

    private final MenuService menuService;
    private final VoteService voteService;
    private final ResultService resultService;
    private final SlackMessageService slackMessageService;
    private final HystecMenuService hystecMenuService;
    private final SlackEventService slackEventService;

    @GetMapping("/test")
    public String sendSlackTest() {
        slackMessageService.sendMessage("테스트 메시지입니다! 👋");
        return "Message Attempted";
    }

    /*
        [GET] /slack/menus -> 오늘의 메뉴 목록 조회
    */
    @GetMapping("/menus")
    public ResponseEntity<List<MenuDTO>> getTodayMenus() {
        List<MenuDTO> menus = menuService.getTodayMenus();
        return ResponseEntity.ok(menus);
    }

    /*
    [POST] /slack/vote -> 사용자 메뉴의 투표 처리
     */

    @PostMapping("/vote")
    public ResponseEntity<String> handleVote(@RequestBody VoteRequestDTO request) {
        try {
            voteService.vote(request);
            return ResponseEntity.ok("투표가 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("엥? 이미 투표하셨어요!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("사용자 또는 메뉴 정보가 유효하지 않습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류 : " + e.getMessage());
        }
    }

    @PostMapping(value = "/command", produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map<String, String>> handleSlashCommand(
            @RequestParam("text") String text,
            @RequestParam("user_id") String userId,
            @RequestParam("user_name") String userName,
            @RequestParam("team_id") String teamId) {

        Map<String, String> response = new HashMap<>();

        try {
            // 사용자가 "A" 등으로 투표했다고 가정
            VoteRequestDTO vote = new VoteRequestDTO(userId, text.trim(), userName, teamId);
            voteService.vote(vote);

            slackEventService.ensureUserName(userId, userName);

            response.put("response_type", "in_channel"); // 채널 전체에 보이도록
            response.put("text", userName + " 님의 투표가 완료되었습니다! ✅");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            response.put("response_type", "ephemeral"); // 사용자에게만 보이도록
            response.put("text", "엥? 이미 투표하셨어요! ❗");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("response_type", "ephemeral");
            response.put("text", "유효하지 않은 사용자나 메뉴입니다. ❌");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("response_type", "ephemeral");
            response.put("text", "서버 오류가 발생했어요: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }



    /*
    [GET] /slack/result -> 오늘의 투표 결과를 Slack 메세지 형식으로 반환
     */
    @GetMapping("/result")
    public ResponseEntity<List<Map<String, Object>>> getTodayVoteResult() {
        VoteSummaryResponse summary = resultService.getVoteSummary(LocalDate.now());
        List<Map<String, Object>> blocks = resultService.formatSummaryForSlack(summary);
        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/result/send")
    public ResponseEntity<String> sendTodayResultToSlack() {
        resultService.sendTodaySummaryToSlack();
        return ResponseEntity.ok("✅ 투표 결과가 Slack에 전송되었습니다!");
    }

    @GetMapping("/menus/bundang/lunch")
    public ResponseEntity<String> bundangLunch() {
        var res = hystecMenuService.fetchBundangBiwonLunch(LocalDate.now());
        String msg = hystecMenuService.formatBundangBiwonLunchForSlack(res);
        // slackMessageService.sendMessage(msg); // 원하면 전송
        return ResponseEntity.ok(msg);
    }


}
