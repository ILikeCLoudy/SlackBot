package com.SKALA.LikeCloudy.DTO.hystec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HystecMenuResponse(
        @JsonProperty("menuList") List<HystecMenuItem> menuList,
        @JsonProperty("WEATHER") String weather,
        @JsonProperty("TEMPERATURE") String temperature,
        @JsonProperty("PRECIPITATION") String precipitation
) {}
