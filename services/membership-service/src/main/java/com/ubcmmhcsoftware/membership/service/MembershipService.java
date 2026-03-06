package com.ubcmmhcsoftware.membership.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.membership.client.UserServiceClient;
import com.ubcmmhcsoftware.membership.dto.CheckoutSessionDTO;
import com.ubcmmhcsoftware.membership.dto.MembershipRegistrationDTO;
import com.ubcmmhcsoftware.membership.entity.Membership;
import com.ubcmmhcsoftware.membership.enums.PaymentMethod;
import com.ubcmmhcsoftware.membership.event.MembershipActivatedEvent;
import com.ubcmmhcsoftware.membership.event.MembershipCreatedEvent;
import com.ubcmmhcsoftware.membership.event.MembershipEventPublisher;
import com.ubcmmhcsoftware.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final StripeService stripeService;
    private final MembershipEventPublisher eventPublisher;
    private final UserServiceClient userServiceClient;

    @Transactional
    public CheckoutSessionDTO createMembership(MembershipRegistrationDTO dto, UUID userId) throws StripeException {
        if (userId != null && !userServiceClient.userExists(userId)) {
            throw new IllegalStateException("User not found. Please log in again.");
        }
        if (membershipRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("A membership already exists for this email");
        }

        PaymentMethod paymentMethod = dto.getPaymentMethod() != null ? dto.getPaymentMethod() : PaymentMethod.STRIPE;

        Membership membership = Membership.builder()
                .userId(userId)
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
            eventPublisher.publishMembershipCreated(MembershipCreatedEvent.builder()
                    .membershipId(membership.getId())
                    .email(dto.getEmail())
                    .newsletterOptIn(true)
                    .build());
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

    @Transactional
    public void activateMembership(String membershipId, String customerId, String subscriptionId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(membershipId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid membership ID format: {}", membershipId);
            return;
        }

        Optional<Membership> optionalMembership = membershipRepository.findById(uuid);

        if (optionalMembership.isEmpty()) {
            log.error("No membership found for ID: {}", membershipId);
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
        membership.setEndDate(now.plusYears(1));

        membershipRepository.save(membership);
        log.info("Activated membership {} for {} via Stripe", membership.getId(), membership.getEmail());

        eventPublisher.publishMembershipActivated(MembershipActivatedEvent.builder()
                .membershipId(membership.getId())
                .email(membership.getEmail())
                .userId(membership.getUserId())
                .paymentMethod(PaymentMethod.STRIPE)
                .build());
    }

    public Optional<Membership> getMembershipByEmail(String email) {
        return membershipRepository.findByEmail(email);
    }

    public boolean hasActiveMembership(String email) {
        return membershipRepository.findByEmail(email)
                .map(Membership::isActive)
                .orElse(false);
    }

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
        membership.setEndDate(now.plusYears(1));

        membershipRepository.save(membership);
        log.info("Manually approved membership {} for {} by admin {} via {}",
                membership.getId(), memberEmail, adminEmail, paymentMethod);

        eventPublisher.publishMembershipActivated(MembershipActivatedEvent.builder()
                .membershipId(membership.getId())
                .email(membership.getEmail())
                .userId(membership.getUserId())
                .paymentMethod(paymentMethod)
                .build());
    }

    public List<Membership> getPendingMemberships() {
        return membershipRepository.findByActiveAndPaymentStatus(false, "pending");
    }
}
