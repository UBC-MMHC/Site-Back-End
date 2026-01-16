package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.NewsletterSubscriber;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.NewsletterRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsletterService {
    private final NewsletterRepository newsletterRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${brevo.api-key}")
    private String brevoApiKey;
    @Value("${brevo.newsletter-list-id}")
    private Integer newsletterListId;
    @Value("${brevo.base-url}")
    private String brevoBaseUrl;

    public void addEmail(String email) {
        // Check if already subscribed locally - if so, just return success (no error)
        NewsletterSubscriber existingSubscriber = this.newsletterRepository.findByEmail(email);
        if (existingSubscriber != null && !existingSubscriber.isUnsubscribed()) {
            // Already subscribed - treat as success, just update User entity if needed
            userRepository.findUserByEmail(email).ifPresent(user -> {
                if (!user.isNewsletterSubscription()) {
                    user.setNewsletterSubscription(true);
                    userRepository.save(user);
                }
            });
            return; // Success - already subscribed
        }

        // Try to add to Brevo (silently skip if it fails)
        try {
            String url = brevoBaseUrl + "/contacts";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey.trim());

            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("listIds", List.of(newsletterListId));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);
        } catch (RestClientException ex) {
            // Log but don't fail - still save to local DB
            System.err.println("Brevo API call failed (continuing anyway): " + ex.getMessage());
        }

        // Save to local database
        if (existingSubscriber == null) {
            NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                    .email(email).createdAt(LocalDateTime.now()).unsubscribed(false).build();
            this.newsletterRepository.save(subscriber);
        }

        // Update User entity if they have an account
        userRepository.findUserByEmail(email).ifPresent(user -> {
            user.setNewsletterSubscription(true);
            userRepository.save(user);
        });
    }

}
