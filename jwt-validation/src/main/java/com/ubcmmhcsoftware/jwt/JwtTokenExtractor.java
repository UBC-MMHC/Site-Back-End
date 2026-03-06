package com.ubcmmhcsoftware.jwt;

/**
 * Extracts JWT token from HTTP request sources: Bearer header or cookie.
 */
public final class JwtTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private JwtTokenExtractor() {
    }

    /**
     * Extracts JWT from Authorization Bearer header or cookie.
     *
     * @param authHeader  Value of Authorization header (may be null).
     * @param cookieValue Value of JWT cookie (may be null).
     * @return The token string, or null if not found.
     */
    public static String extract(String authHeader, String cookieValue) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty()) {
                return token;
            }
        }
        if (cookieValue != null && !cookieValue.isBlank()) {
            return cookieValue.trim();
        }
        return null;
    }

    /**
     * Extracts JWT from Authorization header only.
     */
    public static String fromBearerHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty()) {
                return token;
            }
        }
        return null;
    }

    /**
     * Extracts JWT from cookie value.
     */
    public static String fromCookie(String cookieValue) {
        if (cookieValue != null && !cookieValue.isBlank()) {
            return cookieValue.trim();
        }
        return null;
    }
}
