package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Service.CustomUserDetailsService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * The "Gatekeeper" of the application.
 * <p>
 * This filter intercepts EVERY single HTTP request coming into the server.
 * It checks if the request has a valid "JWT" cookie. If it does, it tells
 * Spring Security:
 * "This user is authenticated, here is their ID and their Roles."
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AppProperties appProperties;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/newsletter/add-email",
            "/api/stripe/webhook",
            "/error");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean excluded = EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
        log.info("JWT Filter - Path: {}, Excluded: {}, Method: {}", path, excluded, request.getMethod());
        return excluded;
    }

    /**
     * The core logic loop.
     *
     * @param request     The incoming HTTP request (headers, cookies, body).
     * @param response    The outgoing HTTP response.
     * @param filterChain The chain of other filters (CORS, CSRF, etc.) that must
     *                    run after this.
     */
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
                List<String> roles = jwtService.extractRoles(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UserDetails userDetails = customUserDetailsService.loadUserById(UUID.fromString(id));

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }

        filterChain.doFilter(request, response);
    }
}
