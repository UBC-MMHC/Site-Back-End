package com.ubcmmhcsoftware.membership.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Value("${membership.events.exchange:membership.events}")
    private String exchange;

    @Value("${membership.events.routing-key.membership-created:membership.created}")
    private String routingKey;

    @Override
    public void publishMembershipCreated(MembershipCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(new CloudEventWrapper(event));
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            log.info("Published MembershipCreated event for membership {} (newsletterOptIn={})",
                    event.getMembershipId(), event.isNewsletterOptIn());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize MembershipCreated event: {}", e.getMessage());
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
}
