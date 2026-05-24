package com.ubcmmhcsoftware.membership.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
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
            if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)
                    || e.getStatusCode().isSameCodeAs(HttpStatus.SERVICE_UNAVAILABLE)) {
                log.warn("User Service returned {} for userId {} — skipping exists check (check INTERNAL_SERVICE_KEY on both services)",
                        e.getStatusCode(), userId);
                return true;
            }
            log.warn("User Service returned {} for userId {}: {}", e.getStatusCode(), userId, e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private boolean userExistsFallback(UUID userId, Exception e) {
        log.warn("User Service unavailable for userId {} ({}); skipping exists check", userId, e.getMessage());
        return true;
    }
}
