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
     * Reads raw request body to ensure payload is unmodified for signature
     * verification.
     *
     * @param request   The HTTP request
     * @param signature The Stripe-Signature header
     * @return 200 OK on success, 400 on signature failure
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String signature) {

        String payload;
        try {
            payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
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
                membershipService.activateMembership(
                        session.getId(),
                        session.getCustomer());
            }
        }

        return ResponseEntity.ok("Received");
    }
}
