package com.SKALA.LikeCloudy.DTO.hystec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)   // <-- 추가
public record HystecMenuItem(
        @JsonProperty("COURSE_NAME") String courseName,
        @JsonProperty("MENU_NAME")  String menuName,
        @JsonProperty("SIDE_1")     String side1,
        @JsonProperty("SIDE_2")     String side2,
        @JsonProperty("SIDE_3")     String side3,
        @JsonProperty("SIDE_4")     String side4,
        @JsonProperty("SIDE_5")     String side5,
        @JsonProperty("SIDE_6")     String side6,
        @JsonProperty("KCAL")       String kcal,
        @JsonProperty("MENU_ORIGIN") String menuOrigin,
        @JsonProperty("AVG_STAR")    String avgStar,
        @JsonProperty("SATI_CNT")    String satiCnt,
        @JsonProperty("SOLDOUT_YN")  String soldoutYn,
        @JsonProperty("MENU_GUIDE")  String menuGuide,
        @JsonProperty("SAVE_FILE_NM") String saveFileNm
) {}