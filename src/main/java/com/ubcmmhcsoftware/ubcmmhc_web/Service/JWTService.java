package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
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
import java.util.function.Function;

/**
 * Service responsible for the lifecycle of JSON Web Tokens (JWT).
 * <p>
 * This service handles the Creation (Signing), Parsing (Decoding), and Validation
 * of tokens used for stateless authentication.
 * </p>
 * */
@Service
public class JWTService {
    // Duration: 7 Days calculated in seconds
    // (7 days * 24 hours * 60 mins * 60 seconds)
    private static final long EXPIRATION_TIME = 7L * 24 * 60 * 60;
    private final SecretKey key;

    /**
     * Constructor that initializes the cryptographic key.
     * * @param secret The secret string from application.properties used to sign tokens.
     * Must be at least 256 bits (32 chars) for HS256 security.
     */
    public JWTService(@Value("${spring.jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT containing the user's ID and Roles.
     *
     *
     * @param userDetails The authenticated user containing the ID and Authorities.
     * @return A String representation of the JWT (Header.Payload.Signature).
     */
    public String generateToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(EXPIRATION_TIME);

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

    /**
     * Extracts a specific claim from the token using a resolver function.
     * <p>
     * This acts as a generic helper to avoid repeating parsing logic.
     * </p>
     *
     * @param token The JWT string.
     * @param claimsResolver A function defining which claim to extract (e.g., Claims::getSubject).
     * @param <T> The type of the claim being returned.
     * @return The extracted claim data.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    /**
     * Validates if a token belongs to the given user and is not expired.
     *
     * @param token The JWT string.
     * @param id The UUID of the user attempting to authenticate.
     * @return true if token is valid and belongs to the user, false otherwise.
     */
    public boolean isTokenValid(String token, String id) {
        String tokenId = extractId(token);
        return (tokenId.equals(id) && !isTokenExpired(token));
    }

    /**
     * Checks if the "exp" (expiration) claim in the token is in the past.
     * * @param token The JWT string.
     * @return true if the current time is after the expiration time.
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    /**
     * Helper to extract the Subject (User ID) from the token.
     * * @param token The JWT string.
     * @return The User ID (UUID) as a String.
     */
    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}