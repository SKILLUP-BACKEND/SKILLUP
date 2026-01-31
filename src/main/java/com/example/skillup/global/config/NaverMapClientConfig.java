package com.example.skillup.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NaverMapClientConfig {
    @Bean
    public WebClient naverMapWebClient(
            WebClient.Builder builder,
            @Value("${map.naver.base-url}") String baseUrl,
            @Value("${map.naver.client.id}") String clientId,
            @Value("${map.naver.client.secret}") String clientSecret
    ) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .build();
    }
}
