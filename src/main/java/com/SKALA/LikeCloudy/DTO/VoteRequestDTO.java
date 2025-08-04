package com.SKALA.LikeCloudy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequestDTO {
    private String slackUserId; // 사용자 식별
    private String menuCode; // A~E중 선택한 메뉴
}
