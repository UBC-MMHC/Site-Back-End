package com.ubcmmhcsoftware.membership.event;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Event published when a membership is created.
 * Newsletter Service subscribes to add email when newsletterOptIn is true.
 */
@Value
@Builder
public class MembershipCreatedEvent {
    UUID membershipId;
    String email;
    boolean newsletterOptIn;
}
