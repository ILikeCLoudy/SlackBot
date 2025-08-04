package com.SKALA.LikeCloudy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class VoteSummaryResponse {
    private List<VoteResultDTO> results; // 메뉴별 집계 결과
    private LocalDate surveyDate; // 투표가 이루어진 날짜
}
