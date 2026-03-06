package com.ubcmmhcsoftware.membership.event;

/**
 * Publishes MembershipCreated events.
 * Implementations: RabbitMQ (prod) or NoOp (tests).
 */
public interface MembershipEventPublisher {
    void publishMembershipCreated(MembershipCreatedEvent event);
}
