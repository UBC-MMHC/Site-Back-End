package com.ubcmmhcsoftware.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates JWTs and extracts claims using the shared secret.
 * <p>
 * Auth Service is the sole issuer. All downstream services (User, Membership, Newsletter)
 * validate tokens using the same JWT_SECRET_TOKEN. No service-to-service call needed.
 * </p>
 */
public class JwtValidator {

    private final SecretKey key;

    /**
     * @param secret The shared JWT secret (JWT_SECRET_TOKEN). Must be at least 256 bits (32 chars) for HS256.
     */
    public JwtValidator(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the token (signature + expiry) and returns the claims.
     *
     * @param token The JWT string (from Bearer header or cookie).
     * @return Validated claims.
     * @throws JwtException if token is invalid, expired, or malformed.
     */
    public JwtClaims validateAndExtract(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.getSubject();
        if (userId == null || userId.isBlank()) {
            throw new JwtException("Missing or empty 'sub' claim");
        }

        String email = claims.get("email", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
            roles = new ArrayList<>();
        }

        return new JwtClaims(userId, email, roles);
    }

    /**
     * Attempts to validate and extract claims. Returns null if token is invalid.
     *
     * @param token The JWT string.
     * @return JwtClaims if valid, null otherwise.
     */
    public JwtClaims validateAndExtractOrNull(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return validateAndExtract(token);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Checks if the token is valid (signature and not expired) without extracting claims.
     */
    public boolean isValid(String token) {
        return validateAndExtractOrNull(token) != null;
    }
}
