package com.ubcmmhcsoftware.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtValidator")
class JwtValidatorTest {

    private static final String SECRET = "256bit-long-secret-for-hs256-algorithm!!";
    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    private JwtValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JwtValidator(SECRET);
    }

    @Test
    @DisplayName("validates and extracts claims from valid token")
    void validatesAndExtracts() {
        String token = createToken(USER_ID, "user@example.com", List.of("ROLE_USER"));

        JwtClaims claims = validator.validateAndExtract(token);

        assertThat(claims.userId()).isEqualTo(USER_ID);
        assertThat(claims.email()).isEqualTo("user@example.com");
        assertThat(claims.roles()).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("returns null for invalid token")
    void returnsNullForInvalidToken() {
        JwtClaims claims = validator.validateAndExtractOrNull("invalid.token.here");
        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("returns null for expired token")
    void returnsNullForExpiredToken() {
        String token = createExpiredToken(USER_ID);
        JwtClaims claims = validator.validateAndExtractOrNull(token);
        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("throws for invalid token when using validateAndExtract")
    void throwsForInvalidToken() {
        assertThatThrownBy(() -> validator.validateAndExtract("bad"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    @DisplayName("isValid returns true for valid token")
    void isValidReturnsTrueForValidToken() {
        String token = createToken(USER_ID, null, List.of("ROLE_USER"));
        assertThat(validator.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("isValid returns false for invalid token")
    void isValidReturnsFalseForInvalidToken() {
        assertThat(validator.isValid("invalid")).isFalse();
    }

    @Test
    @DisplayName("getRolesWithPrefix adds ROLE_ when missing")
    void getRolesWithPrefix() {
        String token = createToken(USER_ID, null, List.of("USER", "ADMIN"));
        JwtClaims claims = validator.validateAndExtract(token);
        assertThat(claims.getRolesWithPrefix()).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    private String createToken(String subject, String email, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        var builder = Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key);
        if (email != null) {
            builder.claim("email", email);
        }
        return builder.compact();
    }

    private String createExpiredToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date(System.currentTimeMillis() - 7200_000))
                .expiration(new Date(System.currentTimeMillis() - 3600_000))
                .signWith(key)
                .compact();
    }
}
