package com.ubcmmhcsoftware.jwt.spring;

import com.ubcmmhcsoftware.jwt.JwtClaims;
import com.ubcmmhcsoftware.jwt.JwtTokenExtractor;
import com.ubcmmhcsoftware.jwt.JwtValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet filter that validates JWT from Bearer header or cookie and sets SecurityContext.
 * <p>
 * Use this in downstream services (User, Membership, Newsletter) when they need to validate
 * JWTs directly (e.g. when called without going through the gateway, or for defense-in-depth).
 * When traffic comes via the gateway, the gateway forwards X-User-Id, X-User-Email, X-User-Roles;
 * this filter validates the JWT when present.
 * </p>
 * <p>
 * Requires: spring-web, spring-security-core, jakarta.servlet-api
 * </p>
 */
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final String jwtCookieName;

    public JwtValidationFilter(JwtValidator jwtValidator, String jwtCookieName) {
        this.jwtValidator = jwtValidator;
        this.jwtCookieName = jwtCookieName != null ? jwtCookieName : "JWT";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String cookieValue = extractCookieValue(request, jwtCookieName);
        String token = JwtTokenExtractor.extract(authHeader, cookieValue);

        if (token != null) {
            JwtClaims claims = jwtValidator.validateAndExtractOrNull(token);
            if (claims != null) {
                List<SimpleGrantedAuthority> authorities = claims.getRolesWithPrefix().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        claims,
                        null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private static String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
