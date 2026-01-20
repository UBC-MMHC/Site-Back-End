package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
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
 * Uses Event Retrieval API instead of signature verification because
 * Railway's proxy modifies the payload, breaking signature verification.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stripe")
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;
    private final MembershipService membershipService;
    private final ObjectMapper objectMapper;

    /**
     * Handles incoming Stripe webhook events.
     * Since Railway's proxy modifies the JSON payload (pretty-printing),
     * we cannot use signature verification. Instead, we:
     * 1. Parse the event ID from the payload
     * 2. Fetch the event directly from Stripe's API
     * This guarantees authenticity since fake event IDs won't exist in Stripe.
     *
     * @param request The HTTP request containing the payload
     * @return 200 OK on success, 400 on failure
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {

        // Read payload
        String payload;
        try {
            byte[] rawBody = request.getInputStream().readAllBytes();
            payload = new String(rawBody, StandardCharsets.UTF_8);
            log.info("[WEBHOOK] Received payload of {} bytes", rawBody.length);
        } catch (IOException e) {
            log.error("Failed to read webhook payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read payload");
        }

        // Parse event ID from payload
        String eventId;
        String eventType;
        try {
            JsonNode json = objectMapper.readTree(payload);
            eventId = json.get("id").asText();
            eventType = json.get("type").asText();

            if (eventId == null || !eventId.startsWith("evt_")) {
                log.error("Invalid event ID in payload: {}", eventId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid event ID");
            }
            log.info("[WEBHOOK] Parsed event: {} ({})", eventType, eventId);
        } catch (Exception e) {
            log.error("Failed to parse event ID from payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        // Retrieve event directly from Stripe API (this verifies authenticity)
        Event event;
        try {
            event = stripeService.retrieveEvent(eventId);
            log.info("[WEBHOOK] Retrieved event from Stripe: {} ({})", event.getType(), event.getId());
        } catch (StripeException e) {
            log.error("Failed to retrieve event {} from Stripe: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event not found in Stripe");
        }

        // Handle checkout.session.completed
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
                log.info("[WEBHOOK] Successfully activated membership {}", membershipId);
            }
        }

        return ResponseEntity.ok("Received");
    }
}
