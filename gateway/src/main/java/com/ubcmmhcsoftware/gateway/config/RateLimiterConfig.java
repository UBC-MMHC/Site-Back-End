package com.ubcmmhcsoftware.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Key resolver for Redis-backed rate limiting.
 * Uses client IP (from X-Forwarded-For when behind proxy) for per-IP limits.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            String ip = xForwardedFor != null && !xForwardedFor.isEmpty()
                    ? xForwardedFor.split(",")[0].trim()
                    : exchange.getRequest().getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                            : "unknown";
            return Mono.just(ip);
        };
    }
}
