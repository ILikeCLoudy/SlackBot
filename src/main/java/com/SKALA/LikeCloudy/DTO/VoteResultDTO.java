package com.SKALA.LikeCloudy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class VoteResultDTO {
    private String menuCode;
    private String menuName;
    private int voteCount;
    private List<String> voters; // 선택한 사용자 이름
}
