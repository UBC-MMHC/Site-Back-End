package com.ubcmmhcsoftware.newsletter.event;

import com.ubcmmhcsoftware.membership.event.MembershipCreatedEvent;
import com.ubcmmhcsoftware.newsletter.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipCreatedEventListener {

    private final NewsletterService newsletterService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMembershipCreated(MembershipCreatedEvent event) {
        if (event == null || !event.isNewsletterOptIn()) {
            return;
        }
        String email = event.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("MembershipCreated event has no email: {}", event.getMembershipId());
            return;
        }
        log.info("Processing newsletter signup from MembershipCreated: membershipId={}, email={}",
                event.getMembershipId(), email);
        newsletterService.addEmail(email);
    }
}
