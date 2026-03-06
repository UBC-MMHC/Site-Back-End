package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Trusts gateway-forwarded headers when present.
 * <p>
 * When the request comes through the gateway, it sets X-User-Id, X-User-Email, X-User-Roles.
 * This filter uses those headers to set SecurityContext—no JWT validation in the backend.
 * When headers are absent (e.g. direct call, local dev without gateway), the request falls
 * through to {@link JWTAuthenticationFilter} for JWT validation.
 * </p>
 */
@Component
public class GatewayTrustFilter extends OncePerRequestFilter {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_EMAIL = "X-User-Email";
    private static final String X_USER_ROLES = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(X_USER_ID);
        if (userId != null && !userId.isBlank()) {
            String email = request.getHeader(X_USER_EMAIL);
            String rolesHeader = request.getHeader(X_USER_ROLES);
            List<GrantedAuthority> authorities = GatewayTrustedPrincipal.parseRoles(rolesHeader);

            GatewayTrustedPrincipal principal = new GatewayTrustedPrincipal(userId, email, authorities);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
