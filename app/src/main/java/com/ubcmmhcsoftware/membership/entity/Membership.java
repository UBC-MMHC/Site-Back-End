package com.ubcmmhcsoftware.membership.entity;

import com.ubcmmhcsoftware.membership.enums.MembershipType;
import com.ubcmmhcsoftware.membership.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a membership registration with Stripe payment integration.
 * No JPA relationship to User; links via optional user_id (UUID) and required email.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "membership")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Optional link to User (from Auth/User service). Null for guest registrations. */
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipType membershipType;

    private String studentId;

    private String instagram;

    private boolean instagramGroupchat;

    private boolean newsletterOptIn;

    private String stripeCustomerId;

    private String stripeSubscriptionId;

    private String stripeSessionId;

    private String paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String approvedBy;

    private LocalDateTime verifiedAt;

    private LocalDateTime endDate;

    private boolean active;
}
