package dev.rangel.statkick.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class FootballApiConfig {

    @Bean
    public RestClient footballApiClient(
            RestClient.Builder builder,
            @Value("${football.api.base-url}") String baseUrl,
            @Value("${football.api.token}") String token
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Auth-Token", token)
                .requestFactory(requestFactory)
                .build();
    }
}