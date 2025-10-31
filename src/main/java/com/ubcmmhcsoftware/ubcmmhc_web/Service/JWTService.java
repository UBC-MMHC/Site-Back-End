package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {
    private static final int secondsToAdd = 60 * 60 * 24 * 24;
    private static SecretKey key;

    public JWTService(@Value("${spring.jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(secondsToAdd);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .claims()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .and()
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

    public boolean isTokenValid(String token, String email) {
        String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}