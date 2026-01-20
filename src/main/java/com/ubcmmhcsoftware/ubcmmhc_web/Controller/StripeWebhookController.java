package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.StripeProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.MembershipService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling Stripe webhook events.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stripe")
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;
    private final MembershipService membershipService;
    private final StripeProperties stripeProperties;

    /**
     * Handles incoming Stripe webhook events.
     *
     * @param payload   The raw request body
     * @param signature The Stripe-Signature header
     * @return 200 OK on success, 400 on signature failure
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        // Debug logging for Railway
        String webhookSecret = stripeProperties.getWebhookSecret();
        log.info("[WEBHOOK DEBUG] Payload length: {}", payload != null ? payload.length() : "null");
        log.info("[WEBHOOK DEBUG] Signature header: {}",
                signature != null ? signature.substring(0, Math.min(50, signature.length())) + "..." : "null");
        log.info("[WEBHOOK DEBUG] Webhook secret configured: {}",
                webhookSecret != null && !webhookSecret.isEmpty()
                        ? "YES (starts with: " + webhookSecret.substring(0, Math.min(10, webhookSecret.length()))
                                + "...)"
                        : "NO/EMPTY");
        log.info("[WEBHOOK DEBUG] First 100 chars of payload: {}",
                payload != null && payload.length() > 0 ? payload.substring(0, Math.min(100, payload.length()))
                        : "empty");

        Event event;
        try {
            event = stripeService.verifyWebhookSignature(payload, signature);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            log.error("[WEBHOOK DEBUG] Full signature: {}", signature);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Received Stripe event: {} ({})", event.getType(), event.getId());

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = stripeService.extractSessionFromEvent(event);
            if (session != null) {
                String membershipId = session.getMetadata().get("membership_id");
                if (membershipId == null) {
                    log.error("No membership_id found in session metadata for session {}", session.getId());
                    return ResponseEntity.ok("Received - no membership_id in metadata");
                }
                membershipService.activateMembership(
                        membershipId,
                        session.getCustomer(),
                        session.getSubscription());
            }
        }

        return ResponseEntity.ok("Received");
    }
}
