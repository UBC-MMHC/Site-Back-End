package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.stripe.exception.StripeException;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.CheckoutSessionDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MembershipRegistrationDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.MembershipService;
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

/**
 * REST controller for membership registration and status.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/membership")
@Slf4j
public class MembershipController {

    private final MembershipService membershipService;

    /**
     * Registers a new membership and returns Stripe checkout URL.
     *
     * @param dto The registration form data
     * @return CheckoutSessionDTO with session URL for frontend redirect
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerMembership(@Valid @RequestBody MembershipRegistrationDTO dto) {
        try {
            CheckoutSessionDTO sessionDTO = membershipService.createMembership(dto);
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

    /**
     * Gets the current user's membership status.
     *
     * @param userDetails The authenticated user
     * @return Membership details if found
     */
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
                "endDate", m.getEndDate() != null ? m.getEndDate().toString() : null,
                "verifiedAt", m.getVerifiedAt() != null ? m.getVerifiedAt().toString() : null));
    }

    /**
     * Checks if an email has an active membership (public endpoint).
     *
     * @param email The email to check
     * @return true if active membership exists
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkMembership(@RequestParam String email) {
        boolean hasActive = membershipService.hasActiveMembership(email);
        return ResponseEntity.ok(Map.of("active", hasActive));
    }

    /**
     * Gets the current authenticated user's membership status.
     * Used by frontend to gate access to dashboard/profile.
     *
     * @param userDetails The authenticated user
     * @return hasMembership (registered), isPaid (payment complete), membershipType
     */
    @GetMapping("/my-status")
    public ResponseEntity<?> getMyMembershipStatus(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Membership> membership = membershipService.getMembershipByEmail(userDetails.getUsername());

        if (membership.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "hasMembership", false,
                    "isPaid", false,
                    "membershipType", (Object) null));
        }

        Membership m = membership.get();
        return ResponseEntity.ok(Map.of(
                "hasMembership", true,
                "isPaid", m.isActive(),
                "membershipType", m.getMembershipType().name(),
                "paymentStatus", m.getPaymentStatus() != null ? m.getPaymentStatus() : "pending"));
    }

    /**
     * Creates a new Stripe checkout session for an existing unpaid membership.
     * Used when user returns to complete payment.
     *
     * @param userDetails The authenticated user
     * @return CheckoutSessionDTO with session URL for frontend redirect
     */
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
