package com.ubcmmhcsoftware.auth.service;

import com.ubcmmhcsoftware.auth.config.AppProperties;
import com.ubcmmhcsoftware.auth.config.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JWTService {
    private static final int MIN_SECRET_LENGTH = 32; // HS256 requires 256 bits

    private final SecretKey key;
    private final AppProperties appProperties;

    public JWTService(@Value("${spring.jwt.secret}") String secret, AppProperties appProperties) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET_TOKEN must be set and non-empty. Set it in Railway Shared Variables or environment.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT_SECRET_TOKEN must be at least 32 characters for HS256. Current length: " + secret.length());
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.appProperties = appProperties;
    }

    public String generateToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(appProperties.getJwtExpirationSeconds());

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userDetails.getId().toString())
                .claim("roles", userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String id) {
        String tokenId = extractId(token);
        return (tokenId.equals(id) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }
}
