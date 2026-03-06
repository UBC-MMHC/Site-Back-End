package com.ubcmmhcsoftware.gateway.filter;

import com.ubcmmhcsoftware.gateway.config.GatewayAppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates JWT from Bearer header or cookie, extracts claims, and forwards them
 * in headers (X-User-Id, X-User-Email, X-User-Roles) to downstream services.
 * Public routes are allowed through without a token.
 */
@Component
@RequiredArgsConstructor
public class JwtClaimForwardingGlobalFilter implements GlobalFilter, Ordered {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_EMAIL = "X-User-Email";
    private static final String X_USER_ROLES = "X-User-Roles";

    private static final List<PathMatcher> PUBLIC_PATHS = List.of(
            path -> path.startsWith("/api/auth/"),
            path -> path.equals("/api/newsletter/add-email"),
            path -> path.equals("/api/membership/register"),
            path -> path.equals("/api/membership/check"),
            path -> path.equals("/api/stripe/webhook"),
            path -> path.startsWith("/login/"),
            path -> path.startsWith("/oauth2/"),
            path -> path.equals("/error")
    );

    private static final List<PathMatcher> PROTECTED_PATHS = List.of(
            path -> path.startsWith("/api/user/"),
            path -> path.startsWith("/api/membership/my-status"),
            path -> path.startsWith("/api/membership/retry-payment"),
            path -> path.startsWith("/api/admin/"),
            path -> path.startsWith("/api/blog/")
    );

    private final JwtDecoder jwtDecoder;
    private final GatewayAppProperties gatewayAppProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return maybeAddClaims(exchange).flatMap(chain::filter);
        }

        String token = extractToken(exchange.getRequest());

        if (token == null) {
            if (isProtectedPath(path)) {
                return unauthorized(exchange.getResponse());
            }
            return chain.filter(exchange);
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            return chain.filter(addClaimHeaders(exchange, jwt));
        } catch (Exception e) {
            // JWT invalid or expired
            if (isProtectedPath(path)) {
                return unauthorized(exchange.getResponse());
            }
            return chain.filter(exchange);
        }
    }

    private Mono<ServerWebExchange> maybeAddClaims(ServerWebExchange exchange) {
        String token = extractToken(exchange.getRequest());
        if (token == null) {
            return Mono.just(exchange);
        }
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return Mono.just(addClaimHeaders(exchange, jwt));
        } catch (Exception e) {
            return Mono.just(exchange);
        }
    }

    private ServerWebExchange addClaimHeaders(ServerWebExchange exchange, Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        @SuppressWarnings("unchecked")
        List<String> roles = jwt.hasClaim("roles") ? jwt.getClaim("roles") : List.of();

        String rolesHeader = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .collect(Collectors.joining(","));

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(X_USER_ID, userId)
                .header(X_USER_EMAIL, email != null ? email : "")
                .header(X_USER_ROLES, rolesHeader)
                .build();

        return exchange.mutate().request(request).build();
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) return token;
        }
        if (request.getCookies().containsKey(gatewayAppProperties.getJwtCookieName())) {
            String cookieValue = request.getCookies().getFirst(gatewayAppProperties.getJwtCookieName()).getValue();
            if (cookieValue != null && !cookieValue.isBlank()) return cookieValue.trim();
        }
        return null;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(m -> m.matches(path));
    }

    private boolean isProtectedPath(String path) {
        return PROTECTED_PATHS.stream().anyMatch(m -> m.matches(path));
    }

    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // Run early in the filter chain
    }

    @FunctionalInterface
    private interface PathMatcher {
        boolean matches(String path);
    }
}
