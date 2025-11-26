package com.ubcmmhcsoftware.ubcmmhc_web.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Represents a temporary security token used for sensitive operations.
 * <p>
 * This entity handles:
 * 1. Password Reset Links
 * 2. Email Verification
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "verification_tokens")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, unique = true)
    private User user;

    private Instant expiryDate;

    /**
     * Custom constructor to generate a token with a specific lifespan.
     *
     * @param token The secret string (usually a UUID or 6-digit code).
     * @param user The user requesting the action.
     * @param expiryTimeInMinutes The lifespan of the token in MINUTES.
     */
    public VerificationToken(String token, User user, int expiryTimeInMinutes) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(expiryTimeInMinutes);
    }

    public void updateToken(String newToken, int expiryTimeInMinutes) {
        this.token = newToken;
        this.expiryDate = calculateExpiryDate(expiryTimeInMinutes);
    }

    private Instant calculateExpiryDate(int expiryTimeInMinutes) {
        return Instant.now().plus(expiryTimeInMinutes, ChronoUnit.MINUTES);
    }
}
