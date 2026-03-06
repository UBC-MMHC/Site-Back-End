package com.ubcmmhcsoftware.membership.service;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.ubcmmhcsoftware.membership.config.AppProperties;
import com.ubcmmhcsoftware.membership.config.StripeProperties;
import com.ubcmmhcsoftware.membership.entity.Membership;
import com.ubcmmhcsoftware.membership.enums.MembershipType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public Event verifyWebhookSignature(String payload, String signature) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
    }

    public Session extractSessionFromEvent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            return (Session) deserializer.getObject().get();
        }

        try {
            return (Session) deserializer.deserializeUnsafe();
        } catch (Exception e) {
            log.error("Failed to deserialize session from raw JSON for event {}: {}", event.getId(), e.getMessage());
        }

        log.error("Failed to deserialize session from event {}", event.getId());
        return null;
    }

    public Event retrieveEvent(String eventId) throws StripeException {
        return Event.retrieve(eventId);
    }

    private String getPriceIdForMembershipType(MembershipType membershipType) {
        return switch (membershipType) {
            case UBC_STUDENT -> stripeProperties.getPrices().getUbcStudent();
            case NON_UBC_STUDENT -> stripeProperties.getPrices().getNonUbcStudent();
            case NON_STUDENT -> stripeProperties.getPrices().getNonStudent();
        };
    }
}
