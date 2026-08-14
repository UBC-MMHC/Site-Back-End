package com.ubcmmhcsoftware.membership.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Default publisher: in-process Spring events.
 * Replace with an AMQP adapter if newsletter is extracted later.
 */
@Component
@RequiredArgsConstructor
public class SpringMembershipEventPublisher implements MembershipEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishMembershipCreated(MembershipCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishMembershipActivated(MembershipActivatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
