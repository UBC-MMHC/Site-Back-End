package com.ubcmmhcsoftware.user.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Secures internal service endpoints with a shared secret.
 * Only requests with valid X-Internal-Service-Key can access /api/user/internal/**.
 */
@Component
@Slf4j
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private static final String INTERNAL_KEY_HEADER = "X-Internal-Service-Key";
    private static final String INTERNAL_PATH_PREFIX = "/api/user/internal/";

    @Value("${app.internal-service-key:}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (expectedKey == null || expectedKey.isBlank()) {
            log.warn("INTERNAL_SERVICE_KEY not configured - internal endpoints are disabled");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Internal API not configured");
            return;
        }

        String providedKey = request.getHeader(INTERNAL_KEY_HEADER);
        if (expectedKey.equals(providedKey)) {
            filterChain.doFilter(request, response);
        } else {
            log.debug("Rejected internal request: invalid or missing X-Internal-Service-Key");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal service key");
        }
    }
}
