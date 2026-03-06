package com.ubcmmhcsoftware.membership.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient for User Service with connection timeout.
 * Resilience4j (retry, circuit breaker) is applied at the client method level.
 */
@Configuration
public class UserServiceClientConfig {

    @Value("${app.user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5));

        return builder
                .baseUrl(userServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
