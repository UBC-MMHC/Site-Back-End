package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.CheckoutSessionDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MembershipRegistrationDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for membership registration business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final StripeService stripeService;
    private final NewsletterService newsletterService;

    /**
     * Creates a new membership registration and initiates Stripe checkout.
     *
     * @param dto The registration form data
     * @return CheckoutSessionDTO with the Stripe session URL
     * @throws StripeException if Stripe API call fails
     */
    @Transactional
    public CheckoutSessionDTO createMembership(MembershipRegistrationDTO dto) throws StripeException {
        // Check if email already has a membership
        if (membershipRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("A membership already exists for this email");
        }

        // Create pending membership
        Membership membership = Membership.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .membershipType(dto.getMembershipType())
                .studentId(dto.getStudentId())
                .instagram(dto.getInstagram())
                .instagramGroupchat(dto.isInstagramGroupchat())
                .newsletterOptIn(dto.isNewsletterOptIn())
                .paymentStatus("pending")
                .active(false)
                .build();

        membership = membershipRepository.save(membership);
        log.info("Created pending membership {} for {}", membership.getId(), dto.getEmail());

        // Handle newsletter subscription if opted in
        if (dto.isNewsletterOptIn()) {
            try {
                newsletterService.addEmail(dto.getEmail());
            } catch (Exception e) {
                log.warn("Failed to subscribe {} to newsletter: {}", dto.getEmail(), e.getMessage());
            }
        }

        // Create Stripe checkout session
        Session session = stripeService.createCheckoutSession(membership);

        // Save Stripe session ID
        membership.setStripeSessionId(session.getId());
        membershipRepository.save(membership);

        return CheckoutSessionDTO.builder()
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }

    /**
     * Activates a membership after successful payment.
     *
     * @param sessionId The Stripe session ID from the webhook
     */
    @Transactional
    public void activateMembership(String sessionId, String customerId, String subscriptionId) {
        Optional<Membership> optionalMembership = membershipRepository.findByStripeSessionId(sessionId);

        if (optionalMembership.isEmpty()) {
            log.error("No membership found for session ID: {}", sessionId);
            return;
        }

        Membership membership = optionalMembership.get();
        LocalDateTime now = LocalDateTime.now();

        membership.setStripeCustomerId(customerId);
        membership.setStripeSubscriptionId(subscriptionId);
        membership.setPaymentStatus("completed");
        membership.setActive(true);
        membership.setVerifiedAt(now);
        membership.setEndDate(now.plusYears(1)); // 1 year membership

        membershipRepository.save(membership);
        log.info("Activated membership {} for {}", membership.getId(), membership.getEmail());
    }

    /**
     * Retrieves membership status by email.
     *
     * @param email The member's email
     * @return Optional containing the Membership if found
     */
    public Optional<Membership> getMembershipByEmail(String email) {
        return membershipRepository.findByEmail(email);
    }

    /**
     * Checks if an email has an active membership.
     *
     * @param email The email to check
     * @return true if active membership exists
     */
    public boolean hasActiveMembership(String email) {
        return membershipRepository.findByEmail(email)
                .map(Membership::isActive)
                .orElse(false);
    }

    /**
     * Creates a new Stripe checkout session for an existing unpaid membership.
     *
     * @param email The user's email
     * @return CheckoutSessionDTO with the Stripe session URL
     * @throws StripeException if Stripe API call fails
     */
    @Transactional
    public CheckoutSessionDTO createRetryPaymentSession(String email) throws StripeException {
        Membership membership = membershipRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("No membership found for this email"));

        if (membership.isActive()) {
            throw new IllegalStateException("Membership is already active");
        }

        Session session = stripeService.createCheckoutSession(membership);

        membership.setStripeSessionId(session.getId());
        membershipRepository.save(membership);

        log.info("Created retry payment session {} for {}", session.getId(), email);

        return CheckoutSessionDTO.builder()
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }
}
