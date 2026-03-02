package com.ubcmmhcsoftware.ubcmmhc_web.Entity;

import com.ubcmmhcsoftware.ubcmmhc_web.Enum.MembershipType;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a membership registration with Stripe payment integration.
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Registration form fields
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

    // Stripe integration fields
    private String stripeCustomerId;

    private String stripeSubscriptionId;

    private String stripeSessionId;

    private String paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String approvedBy; // Admin email who manually approved (null if Stripe)

    // Membership dates
    private LocalDateTime verifiedAt;

    private LocalDateTime endDate;

    private boolean active;
}