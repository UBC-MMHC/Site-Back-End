package com.ubcmmhcsoftware.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "verification_token")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, unique = true)
    private User user;

    private Instant expiryDate;

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
