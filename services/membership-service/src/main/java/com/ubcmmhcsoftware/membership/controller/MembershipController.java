package com.ubcmmhcsoftware.membership.controller;

import com.stripe.exception.StripeException;
import com.ubcmmhcsoftware.membership.dto.CheckoutSessionDTO;
import com.ubcmmhcsoftware.membership.dto.MembershipRegistrationDTO;
import com.ubcmmhcsoftware.membership.entity.Membership;
import com.ubcmmhcsoftware.membership.filter.GatewayClaimAuthenticationFilter.GatewayUser;
import com.ubcmmhcsoftware.membership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/membership")
@Slf4j
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping("/register")
    public ResponseEntity<?> registerMembership(@Valid @RequestBody MembershipRegistrationDTO dto,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = null;
        if (userDetails instanceof GatewayUser gu) {
            try {
                userId = gu.userId() != null ? UUID.fromString(gu.userId()) : null;
            } catch (IllegalArgumentException ignored) {
            }
        }

        try {
            CheckoutSessionDTO sessionDTO = membershipService.createMembership(dto, userId);
            return ResponseEntity.ok(sessionDTO);
        } catch (IllegalStateException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            log.error("Stripe error during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment service unavailable. Please try again."));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getMembershipStatus(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Membership> membership = membershipService.getMembershipByEmail(userDetails.getUsername());

        if (membership.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No membership found"));
        }

        Membership m = membership.get();
        return ResponseEntity.ok(Map.of(
                "active", m.isActive(),
                "membershipType", m.getMembershipType().name(),
                "endDate", m.getEndDate() != null ? m.getEndDate().toString() : "",
                "verifiedAt", m.getVerifiedAt() != null ? m.getVerifiedAt().toString() : ""));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkMembership(@RequestParam String email) {
        boolean hasActive = membershipService.hasActiveMembership(email);
        return ResponseEntity.ok(Map.of("active", hasActive));
    }

    @GetMapping("/my-status")
    public ResponseEntity<?> getMyMembershipStatus(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Membership> membership = membershipService.getMembershipByEmail(userDetails.getUsername());

        if (membership.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "hasMembership", false,
                    "isPaid", false));
        }

        Membership m = membership.get();
        return ResponseEntity.ok(Map.of(
                "hasMembership", true,
                "isPaid", m.isActive(),
                "membershipType", m.getMembershipType().name(),
                "paymentStatus", m.getPaymentStatus() != null ? m.getPaymentStatus() : "pending"));
    }

    @PostMapping("/retry-payment")
    public ResponseEntity<?> retryPayment(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CheckoutSessionDTO sessionDTO = membershipService.createRetryPaymentSession(userDetails.getUsername());
            return ResponseEntity.ok(sessionDTO);
        } catch (IllegalStateException e) {
            log.warn("Retry payment failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            log.error("Stripe error during retry payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment service unavailable. Please try again."));
        }
    }
}
