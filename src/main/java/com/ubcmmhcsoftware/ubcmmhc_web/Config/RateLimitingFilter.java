package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filter that enforces a strict limit on the number of requests a client can make.
 * <p>
 * This implements a "Fixed Window" rate limiting algorithm.
 * It identifies clients by their IP address and stores a counter in memory.
 * </p>
 */
@Component
public class RateLimitingFilter implements Filter {
    // Strict Limit: 5 requests every 5 minutes
    private static final int MAX_REQUESTS_PER_WINDOW = 8;
    private static final long TIME_WINDOW_MS = 300_000; // 5 minutes in milliseconds

    private static class RequestCounter {
        public final AtomicInteger count = new AtomicInteger(0);
        public long windowStartTimestamp = System.currentTimeMillis();
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

        String clientIp = httpRequest.getRemoteAddr();

        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());

        long currentTime = System.currentTimeMillis();
        long windowStart = counter.windowStartTimestamp;

        if (currentTime - windowStart > TIME_WINDOW_MS) {
            counter.count.set(1);
            counter.windowStartTimestamp = currentTime;
        } else {
            counter.count.incrementAndGet();
        }

        if (counter.count.get() > MAX_REQUESTS_PER_WINDOW) {
            // Standard cose is 429 but SC_ has none
            httpResponse.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
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

        // Optional: Log for debugging
        // System.out.println("Rate Limit Cleanup: Removed inactive IPs. Current cache size: " + requestCounts.size());
    }
}
