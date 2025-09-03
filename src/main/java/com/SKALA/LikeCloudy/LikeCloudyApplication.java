package com.SKALA.LikeCloudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LikeCloudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LikeCloudyApplication.class, args);
	}

}
