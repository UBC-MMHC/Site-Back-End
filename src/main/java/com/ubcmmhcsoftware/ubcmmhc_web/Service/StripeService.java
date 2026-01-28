package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.stripe.Stripe;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.StripeProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.MembershipType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling Stripe API interactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final StripeProperties stripeProperties;
    private final AppProperties appProperties;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
    }

    /**
     * Creates a Stripe Checkout Session for membership payment.
     *
     * @param membership The membership entity with registration details
     * @return The created Stripe Session
     * @throws StripeException if Stripe API call fails
     */
    public Session createCheckoutSession(Membership membership) throws StripeException {
        String priceId = getPriceIdForMembershipType(membership.getMembershipType());
        String frontendUrl = appProperties.getFrontendUrl();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(membership.getEmail())
                .setSuccessUrl(frontendUrl + "/membership/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/membership/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build())
                .putMetadata("membership_id", membership.getId().toString())
                .putMetadata("full_name", membership.getFullName())
                .putMetadata("membership_type", membership.getMembershipType().name())
                .build();

        try {
            Session session = Session.create(params);
            log.info("Created Stripe checkout session {} for membership {}", session.getId(), membership.getId());
            return session;
        } catch (CardException e) {
            log.error("Card error: {} - {}", e.getCode(), e.getMessage());
            throw e;
        } catch (RateLimitException e) {
            log.warn("Rate limited by Stripe, requestId={}", e.getRequestId());
            throw e;
        } catch (InvalidRequestException e) {
            log.error("Invalid request to Stripe: {} (param: {})", e.getMessage(), e.getParam());
            throw e;
        } catch (AuthenticationException e) {
            log.error("Stripe authentication failed - check API keys!");
            throw e;
        } catch (ApiConnectionException e) {
            log.error("Network error connecting to Stripe: {}", e.getMessage());
            throw e;
        } catch (StripeException e) {
            log.error("Generic Stripe error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Verifies a Stripe webhook signature and parses the event.
     *
     * @param payload   The raw request body
     * @param signature The Stripe-Signature header value
     * @return The verified Stripe Event
     * @throws SignatureVerificationException if signature is invalid
     */
    public Event verifyWebhookSignature(String payload, String signature) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
    }

    /**
     * Extracts the Session object from a checkout.session.completed event.
     *
     * @param event The Stripe event
     * @return The Session object, or null if deserialization fails
     */
    public Session extractSessionFromEvent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        // Try direct deserialization first
        if (deserializer.getObject().isPresent()) {
            return (Session) deserializer.getObject().get();
        }

        // Fallback: use unsafe deserialization (ignores API version mismatch)
        try {
            return (Session) deserializer.deserializeUnsafe();
        } catch (Exception e) {
            log.error("Failed to deserialize session from raw JSON for event {}: {}", event.getId(), e.getMessage());
        }

        log.error("Failed to deserialize session from event {}", event.getId());
        return null;
    }

    /**
     * Retrieves an event directly from Stripe's API.
     * Use this as an alternative to signature verification when the hosting
     * platform
     * modifies the request payload (like Railway's proxy).
     *
     * @param eventId The Stripe event ID (starts with "evt_")
     * @return The Event from Stripe's API
     * @throws StripeException if the event doesn't exist or API call fails
     */
    public Event retrieveEvent(String eventId) throws StripeException {
        return Event.retrieve(eventId);
    }

    /**
     * Gets the Stripe Price ID for a given membership type.
     */
    private String getPriceIdForMembershipType(MembershipType membershipType) {
        return switch (membershipType) {
            case UBC_STUDENT -> stripeProperties.getPrices().getUbcStudent();
            case NON_UBC_STUDENT -> stripeProperties.getPrices().getNonUbcStudent();
            case NON_STUDENT -> stripeProperties.getPrices().getNonStudent();
        };
    }
}
