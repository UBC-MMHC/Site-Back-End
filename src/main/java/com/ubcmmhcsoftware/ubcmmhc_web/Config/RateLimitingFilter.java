package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Limits # of api requests a user can make per minute
@Component
public class RateLimitingFilter implements Filter {
    private static final int MAX_REQUESTS_PER_WINDOW = 5; // 5 requests
    private static final long TIME_WINDOW_MS = 300_000; // 5 minute

    private static class RequestCounter {
        public final AtomicInteger count = new AtomicInteger(0);
        public long windowStartTimestamp = System.currentTimeMillis();
    }

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

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
}
