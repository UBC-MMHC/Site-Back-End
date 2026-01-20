package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.MembershipService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controller for handling Stripe webhook events.
 * Uses HttpServletRequest to read raw bytes, preventing payload modification.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stripe")
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;
    private final MembershipService membershipService;

    /**
     * Handles incoming Stripe webhook events.
     * Reads raw request body to preserve exact payload for signature verification.
     *
     * @param request   The HTTP request containing the raw payload
     * @param signature The Stripe-Signature header
     * @return 200 OK on success, 400 on signature failure
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String signature) {

        // Read raw payload bytes to preserve exact content
        String payload;
        try {
            byte[] rawBody = request.getInputStream().readAllBytes();
            payload = new String(rawBody, StandardCharsets.UTF_8);
            log.info("[WEBHOOK] Received payload of {} bytes", rawBody.length);
            log.info("[WEBHOOK] First 200 chars: {}", payload.substring(0, Math.min(200, payload.length())));
        } catch (IOException e) {
            log.error("Failed to read webhook payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read payload");
        }

        Event event;
        try {
            event = stripeService.verifyWebhookSignature(payload, signature);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
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
