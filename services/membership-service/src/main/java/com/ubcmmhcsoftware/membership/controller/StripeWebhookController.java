package com.ubcmmhcsoftware.membership.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.ubcmmhcsoftware.membership.service.MembershipService;
import com.ubcmmhcsoftware.membership.service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handles Stripe webhook events.
 * Uses Event Retrieval API when Railway's proxy modifies the payload.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stripe")
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;
    private final MembershipService membershipService;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        String payload;
        try {
            byte[] rawBody = request.getInputStream().readAllBytes();
            payload = new String(rawBody, StandardCharsets.UTF_8);
            log.info("[WEBHOOK] Received payload of {} bytes", rawBody.length);
        } catch (IOException e) {
            log.error("Failed to read webhook payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read payload");
        }

        String eventId;
        try {
            JsonNode json = objectMapper.readTree(payload);
            eventId = json.get("id").asText();
            String eventType = json.get("type").asText();

            if (eventId == null || !eventId.startsWith("evt_")) {
                log.error("Invalid event ID in payload: {}", eventId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid event ID");
            }
            log.info("[WEBHOOK] Parsed event: {} ({})", eventType, eventId);
        } catch (Exception e) {
            log.error("Failed to parse event ID from payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        Event event;
        try {
            event = stripeService.retrieveEvent(eventId);
            log.info("[WEBHOOK] Retrieved event from Stripe: {} ({})", event.getType(), event.getId());
        } catch (StripeException e) {
            log.error("Failed to retrieve event {} from Stripe: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event not found in Stripe");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = stripeService.extractSessionFromEvent(event);
            if (session != null) {
                String membershipId = session.getMetadata().get("membership_id");
                if (membershipId == null) {
                    log.error("No membership_id found in session metadata for session {}", session.getId());
                    return ResponseEntity.ok("Received - no membership_id in metadata");
                }
                String customerId = session.getCustomer();
                String subscriptionId = session.getSubscription();
                membershipService.activateMembership(membershipId, customerId, subscriptionId);
                log.info("[WEBHOOK] Successfully activated membership {}", membershipId);
            }
        }

        return ResponseEntity.ok("Received");
    }
}
