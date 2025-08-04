package com.SKALA.LikeCloudy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserDTO {
    private String slackUserId;
    private String slackUserName; // 이름도 저장?
    private String teamId; // 팀정보 저장
}
