package com.SKALA.LikeCloudy.Config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class HystecClientConfig {

    @Bean
    public WebClient hystecWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .compress(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS));
                });

        return builder
                .baseUrl("https://mc.skhystec.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ORIGIN,  "https://mc.skhystec.com")
                .defaultHeader(HttpHeaders.REFERER, "https://mc.skhystec.com/V3/menu.html")
                .defaultHeader("X-Requested-With", "XMLHttpRequest")
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build();
    }
}
