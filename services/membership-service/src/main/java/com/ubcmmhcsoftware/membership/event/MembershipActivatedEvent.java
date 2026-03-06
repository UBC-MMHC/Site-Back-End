package com.ubcmmhcsoftware.membership.event;

import com.ubcmmhcsoftware.membership.enums.PaymentMethod;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Event published when a membership is activated (Stripe payment or manual approval).
 * Schema: contracts/schemas/events/membership-activated.json
 */
@Value
@Builder
public class MembershipActivatedEvent {
    UUID membershipId;
    String email;
    UUID userId;
    PaymentMethod paymentMethod;
}
