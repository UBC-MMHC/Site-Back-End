package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filter that enforces a strict limit on the number of requests a client can
 * make.
 * <p>
 * This implements a "Fixed Window" rate limiting algorithm.
 * It identifies clients by their IP address and stores a counter in memory.
 * </p>
 */
@Component
public class RateLimitingFilter implements Filter {
    // Strict Limit: 20 requests every 5 minutes
    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private static final long TIME_WINDOW_MS = 300_000; // 5 minutes in milliseconds

    // Paths to exclude from rate limiting (webhook endpoints)
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/stripe/webhook");

    private static class RequestCounter {
        public final AtomicInteger count = new AtomicInteger(0);
        public volatile long windowStartTimestamp = System.currentTimeMillis();
    }

    // Ram ISSUE if not handled or cleaned properly
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    /**
     * Intercepts the request to check if the user has exceeded their quota.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();

        if (EXCLUDED_PATHS.stream().anyMatch(requestPath::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = httpRequest.getRemoteAddr();
        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());

        long currentTime = System.currentTimeMillis();

        synchronized (counter) {
            if (currentTime - counter.windowStartTimestamp > TIME_WINDOW_MS) {
                counter.count.set(1);
                counter.windowStartTimestamp = currentTime;
            } else {
                counter.count.incrementAndGet();
            }
        }

        int currentCount = counter.count.get();
        int remaining = Math.max(0, MAX_REQUESTS_PER_WINDOW - currentCount);

        httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_WINDOW));
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        if (currentCount > MAX_REQUESTS_PER_WINDOW) {
            long retryAfterSeconds = (TIME_WINDOW_MS - (currentTime - counter.windowStartTimestamp)) / 1000;
            httpResponse.setStatus(429);
            httpResponse.setHeader("Retry-After", String.valueOf(Math.max(1, retryAfterSeconds)));
            httpResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * CLEANUP TASK
     * This method runs automatically every 10 minutes.
     * It removes IPs that have been inactive for longer than the time window.
     */
    @Scheduled(fixedRate = 600000) // 600,000ms = 10 Minutes
    public void cleanupExpiredIPs() {
        long now = System.currentTimeMillis();

        requestCounts.entrySet().removeIf(entry -> {
            long lastActive = entry.getValue().windowStartTimestamp;

            return (now - lastActive) > TIME_WINDOW_MS;
        });
    }
}
