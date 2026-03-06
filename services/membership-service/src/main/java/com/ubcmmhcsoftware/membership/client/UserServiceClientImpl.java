package com.ubcmmhcsoftware.membership.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

/**
 * REST client for User Service (Phase 3.1).
 * Uses Resilience4j for timeouts, retries, and circuit breaker.
 */
@Component
@Profile("!local")
@RequiredArgsConstructor
@Slf4j
public class UserServiceClientImpl implements UserServiceClient {

    private final WebClient userServiceWebClient;

    @Value("${app.user-service.internal-key:}")
    private String internalServiceKey;

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "userExistsFallback")
    @Retry(name = "userService")
    public boolean userExists(UUID userId) {
        if (userId == null) {
            return false;
        }
        try {
            userServiceWebClient.get()
                    .uri("/api/user/internal/exists/{userId}", userId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (WebClientResponseException e) {
            log.warn("User Service returned {} for userId {}: {}", e.getStatusCode(), userId, e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private boolean userExistsFallback(UUID userId, Exception e) {
        log.warn("User Service unavailable (circuit open or retries exhausted) for userId {}: {}", userId, e.getMessage());
        throw new IllegalStateException("User service temporarily unavailable. Please try again later.");
    }
}
