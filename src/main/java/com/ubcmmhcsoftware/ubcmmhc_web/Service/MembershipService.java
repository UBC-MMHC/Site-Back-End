package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.CheckoutSessionDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MembershipRegistrationDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.PaymentMethod;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
     * Creates a new membership registration.
     * For STRIPE payments, creates a Stripe checkout session.
     * For other payment methods (CASH, ETRANSFER, OTHER), creates membership in
     * pending state.
     *
     * @param dto The registration form data
     * @return CheckoutSessionDTO with the Stripe session URL (STRIPE) or null
     *         session (other methods)
     * @throws StripeException if Stripe API call fails (STRIPE only)
     */
    @Transactional
    public CheckoutSessionDTO createMembership(MembershipRegistrationDTO dto) throws StripeException {
        if (membershipRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("A membership already exists for this email");
        }

        PaymentMethod paymentMethod = dto.getPaymentMethod() != null ? dto.getPaymentMethod() : PaymentMethod.STRIPE;

        Membership membership = Membership.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .membershipType(dto.getMembershipType())
                .studentId(dto.getStudentId())
                .instagram(dto.getInstagram())
                .instagramGroupchat(dto.isInstagramGroupchat())
                .newsletterOptIn(dto.isNewsletterOptIn())
                .paymentMethod(paymentMethod)
                .paymentStatus("pending")
                .active(false)
                .build();

        membership = membershipRepository.save(membership);
        log.info("Created pending membership {} for {} with payment method {}",
                membership.getId(), dto.getEmail(), paymentMethod);

        if (dto.isNewsletterOptIn()) {
            try {
                newsletterService.addEmail(dto.getEmail());
            } catch (Exception e) {
                log.warn("Failed to subscribe {} to newsletter: {}", dto.getEmail(), e.getMessage());
            }
        }

        if (paymentMethod == PaymentMethod.STRIPE) {
            Session session = stripeService.createCheckoutSession(membership);

            membership.setStripeSessionId(session.getId());
            membershipRepository.save(membership);

            return CheckoutSessionDTO.builder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        }

        log.info("Membership {} created with {} payment - awaiting admin approval",
                membership.getId(), paymentMethod);
        return CheckoutSessionDTO.builder()
                .sessionId(null)
                .sessionUrl(null)
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
        membership.setPaymentMethod(PaymentMethod.STRIPE);
        membership.setActive(true);
        membership.setVerifiedAt(now);
        membership.setEndDate(now.plusYears(1)); // 1 year membership

        membershipRepository.save(membership);
        log.info("Activated membership {} for {} via Stripe", membership.getId(), membership.getEmail());
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

    /**
     * Manually approves a membership (for cash/e-transfer payments).
     *
     * @param memberEmail   The member's email to approve
     * @param paymentMethod The payment method used (CASH, ETRANSFER, OTHER)
     * @param adminEmail    The admin who is approving
     */
    @Transactional
    public void manuallyApproveMembership(String memberEmail, PaymentMethod paymentMethod, String adminEmail) {
        Membership membership = membershipRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new IllegalStateException("No membership found for email: " + memberEmail));

        if (membership.isActive()) {
            throw new IllegalStateException("Membership is already active");
        }

        LocalDateTime now = LocalDateTime.now();

        membership.setPaymentStatus("completed");
        membership.setPaymentMethod(paymentMethod);
        membership.setApprovedBy(adminEmail);
        membership.setActive(true);
        membership.setVerifiedAt(now);
        membership.setEndDate(now.plusYears(1)); // 1 year membership

        membershipRepository.save(membership);
        log.info("Manually approved membership {} for {} by admin {} via {}",
                membership.getId(), memberEmail, adminEmail, paymentMethod);
    }

    /**
     * Gets all pending (unpaid) memberships for admin review.
     *
     * @return List of pending memberships
     */
    public List<Membership> getPendingMemberships() {
        return membershipRepository.findByActiveAndPaymentStatus(false, "pending");
    }
}
