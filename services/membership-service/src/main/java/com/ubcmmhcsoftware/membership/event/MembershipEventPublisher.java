package com.ubcmmhcsoftware.membership.event;

/**
 * Publishes membership events.
 * Implementations: RabbitMQ (prod) or NoOp (tests).
 */
public interface MembershipEventPublisher {
    void publishMembershipCreated(MembershipCreatedEvent event);

    void publishMembershipActivated(MembershipActivatedEvent event);
}
