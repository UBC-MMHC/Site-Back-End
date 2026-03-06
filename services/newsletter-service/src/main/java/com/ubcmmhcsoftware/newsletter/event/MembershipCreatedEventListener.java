package com.ubcmmhcsoftware.newsletter.event;

import com.ubcmmhcsoftware.newsletter.config.RabbitMQConfig;
import com.ubcmmhcsoftware.newsletter.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipCreatedEventListener {

    private final NewsletterService newsletterService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NEWSLETTER_MEMBERSHIP_CREATED)
    public void handleMembershipCreated(MembershipCreatedEvent event) {
        if (event == null || event.getData() == null) {
            log.warn("Received null or invalid MembershipCreated event");
            return;
        }

        var data = event.getData();
        if (!data.isNewsletterOptIn()) {
            log.debug("Membership created without newsletter opt-in, skipping: {}", data.getMembershipId());
            return;
        }

        String email = data.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("MembershipCreated event has no email: {}", data.getMembershipId());
            return;
        }

        log.info("Processing newsletter signup from MembershipCreated: membershipId={}, email={}",
                data.getMembershipId(), email);
        newsletterService.addEmail(email);
    }
}
