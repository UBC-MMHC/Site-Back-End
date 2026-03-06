package com.ubcmmhcsoftware.jwt;

import java.util.Collections;
import java.util.List;

/**
 * Immutable DTO containing validated JWT claims.
 * <p>
 * Aligns with {@code contracts/schemas/jwt-claims.json}: sub (userId), email, roles, exp, iat.
 * Auth Service is the sole issuer; all services validate using JWT_SECRET_TOKEN.
 * </p>
 */
public record JwtClaims(
        String userId,
        String email,
        List<String> roles
) {
    /**
     * Creates JwtClaims with a defensive copy of roles.
     */
    public JwtClaims {
        roles = roles != null ? List.copyOf(roles) : Collections.emptyList();
    }

    /**
     * Returns roles with ROLE_ prefix if not already present (for Spring Security compatibility).
     */
    public List<String> getRolesWithPrefix() {
        return roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .toList();
    }
}
