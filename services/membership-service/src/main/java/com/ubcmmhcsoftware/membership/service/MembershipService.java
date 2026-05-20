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
import java.util.Locale;
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

        String email = normalizeEmail(dto.getEmail());
        List<Membership> matches = membershipRepository.findAllByEmailIgnoreCase(email);
        if (!matches.isEmpty()) {
            Membership toResume;
            if (matches.size() == 1) {
                toResume = matches.get(0);
            } else {
                List<Membership> activeMemberships = matches.stream().filter(Membership::isActive).toList();
                if (activeMemberships.size() == 1) {
                    toResume = activeMemberships.get(0);
                } else {
                    throw new IllegalStateException("Multiple memberships found for this email; please contact support");
                }
            }
            return resumeUnpaidMembership(toResume, dto, userId, email);
        }

        PaymentMethod paymentMethod = dto.getPaymentMethod() != null ? dto.getPaymentMethod() : PaymentMethod.STRIPE;

        Membership membership = Membership.builder()
                .userId(userId)
                .fullName(dto.getFullName())
                .email(email)
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
                membership.getId(), email, paymentMethod);

        if (dto.isNewsletterOptIn()) {
            eventPublisher.publishMembershipCreated(MembershipCreatedEvent.builder()
                    .membershipId(membership.getId())
                    .email(email)
                    .newsletterOptIn(true)
                    .build());
        }

        return completeRegistration(membership, paymentMethod);
    }

    private CheckoutSessionDTO resumeUnpaidMembership(Membership membership,
                                                      MembershipRegistrationDTO dto,
                                                      UUID userId,
                                                      String email) throws StripeException {
        boolean previouslyNewsletterOptedIn = membership.isNewsletterOptIn();

        if (membership.isActive()) {
            throw new IllegalStateException("A membership already exists for this email");
        }
        if (userId != null && membership.getUserId() != null && !membership.getUserId().equals(userId)) {
            throw new IllegalStateException("A membership already exists for this email");
        }

        if (userId != null && membership.getUserId() == null) {
            membership.setUserId(userId);
        }

        membership.setFullName(dto.getFullName());
        membership.setEmail(email);
        membership.setMembershipType(dto.getMembershipType());
        membership.setStudentId(dto.getStudentId());
        membership.setInstagram(dto.getInstagram());
        membership.setInstagramGroupchat(dto.isInstagramGroupchat());
        membership.setNewsletterOptIn(dto.isNewsletterOptIn());

        PaymentMethod paymentMethod = dto.getPaymentMethod() != null ? dto.getPaymentMethod() : PaymentMethod.STRIPE;
        membership.setPaymentMethod(paymentMethod);
        membership = membershipRepository.save(membership);

        if (!previouslyNewsletterOptedIn && dto.isNewsletterOptIn()) {
            eventPublisher.publishMembershipCreated(MembershipCreatedEvent.builder()
                    .membershipId(membership.getId())
                    .email(email)
                    .newsletterOptIn(true)
                    .build());
        }

        log.info("Resuming unpaid membership {} for {}", membership.getId(), email);
        return completeRegistration(membership, paymentMethod);
    }

    private CheckoutSessionDTO completeRegistration(Membership membership,
                                                    PaymentMethod paymentMethod) throws StripeException {
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

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
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
        return membershipRepository.findByEmailIgnoreCase(normalizeEmail(email));
    }

    public boolean hasActiveMembership(String email) {
        return membershipRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .map(Membership::isActive)
                .orElse(false);
    }

    @Transactional
    public CheckoutSessionDTO createRetryPaymentSession(String email) throws StripeException {
        Membership membership = membershipRepository.findByEmailIgnoreCase(normalizeEmail(email))
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
        Membership membership = membershipRepository.findByEmailIgnoreCase(normalizeEmail(memberEmail))
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
