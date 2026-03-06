package com.ubcmmhcsoftware.membership.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Publishes MembershipCreated events to RabbitMQ.
 * Newsletter Service (Phase 2.4) will consume these events.
 */
@Component
@Primary
@Profile("!local")
@RequiredArgsConstructor
@Slf4j
public class RabbitMQMembershipEventPublisher implements MembershipEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${membership.events.exchange:membership.events}")
    private String exchange;

    @Value("${membership.events.routing-key.membership-created:membership.created}")
    private String routingKeyMembershipCreated;

    @Value("${membership.events.routing-key.membership-activated:membership.activated}")
    private String routingKeyMembershipActivated;

    @Override
    public void publishMembershipCreated(MembershipCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKeyMembershipCreated, new CloudEventWrapper(event));
            log.info("Published MembershipCreated event for membership {} (newsletterOptIn={})",
                    event.getMembershipId(), event.isNewsletterOptIn());
        } catch (Exception e) {
            log.error("Failed to publish MembershipCreated event: {}", e.getMessage());
        }
    }

    @Override
    public void publishMembershipActivated(MembershipActivatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKeyMembershipActivated, new MembershipActivatedCloudEvent(event));
            log.info("Published MembershipActivated event for membership {}", event.getMembershipId());
        } catch (Exception e) {
            log.error("Failed to publish MembershipActivated event: {}", e.getMessage());
        }
    }

    public record CloudEventWrapper(
            String specversion,
            String type,
            String source,
            EventData data
    ) {
        public CloudEventWrapper(MembershipCreatedEvent event) {
            this(
                    "1.0",
                    "com.ubcmmhc.membership.created",
                    "/membership-service",
                    new EventData(
                            event.getMembershipId().toString(),
                            event.getEmail(),
                            event.isNewsletterOptIn()
                    )
            );
        }
    }

    public record EventData(String membershipId, String email, boolean newsletterOptIn) {}

    public record MembershipActivatedCloudEvent(
            String specversion,
            String type,
            String source,
            MembershipActivatedEventData data
    ) {
        public MembershipActivatedCloudEvent(MembershipActivatedEvent event) {
            this(
                    "1.0",
                    "com.ubcmmhc.membership.activated",
                    "/membership-service",
                    new MembershipActivatedEventData(
                            event.getMembershipId().toString(),
                            event.getEmail(),
                            event.getUserId() != null ? event.getUserId().toString() : null,
                            event.getPaymentMethod() != null ? event.getPaymentMethod().name() : "STRIPE"
                    )
            );
        }
    }

    public record MembershipActivatedEventData(String membershipId, String email, String userId, String paymentMethod) {}
}
