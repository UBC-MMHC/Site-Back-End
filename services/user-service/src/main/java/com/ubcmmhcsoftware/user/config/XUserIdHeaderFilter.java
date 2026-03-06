package com.ubcmmhcsoftware.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Validates that X-User-Id header is present for protected paths.
 * Gateway forwards JWT-validated claims; we trust the gateway and require the header.
 */
public class XUserIdHeaderFilter extends OncePerRequestFilter {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_ROLES = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(X_USER_ID);
        if (userId == null || userId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String rolesHeader = request.getHeader(X_USER_ROLES);
        List<SimpleGrantedAuthority> authorities = rolesHeader != null && !rolesHeader.isBlank()
                ? Stream.of(rolesHeader.split(","))
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList()
                : Collections.emptyList();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
