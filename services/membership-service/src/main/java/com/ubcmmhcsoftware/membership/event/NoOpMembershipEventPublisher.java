package com.ubcmmhcsoftware.membership.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * No-op implementation for local/test profile when RabbitMQ is not available.
 */
@Component
@Profile("local")
@Slf4j
public class NoOpMembershipEventPublisher implements MembershipEventPublisher {

    @Override
    public void publishMembershipCreated(MembershipCreatedEvent event) {
        log.debug("NoOp: would publish MembershipCreated for {} (newsletterOptIn={})",
                event.getMembershipId(), event.isNewsletterOptIn());
    }
}
