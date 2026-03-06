package com.ubcmmhcsoftware.auth.config;

import com.ubcmmhcsoftware.auth.service.CustomUserDetailsService;
import com.ubcmmhcsoftware.auth.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AppProperties appProperties;

    private static final List<String> EXCLUDED_PATHS = List.of();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null && request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (appProperties.getJwtCookieName().equals(c.getName())) {
                    token = c.getValue();
                }
            }
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String id = jwtService.extractId(token);
            if (id != null && jwtService.isTokenValid(token, id)) {
                UserDetails userDetails = customUserDetailsService.loadUserById(UUID.fromString(id));

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }

        filterChain.doFilter(request, response);
    }
}
