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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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
        NewsletterSubscriber existingSubscriber = this.newsletterRepository.findByEmail(email);
        if (existingSubscriber != null && !existingSubscriber.isUnsubscribed()) {
            userRepository.findUserByEmail(email).ifPresent(user -> {
                if (!user.isNewsletterSubscription()) {
                    user.setNewsletterSubscription(true);
                    userRepository.save(user);
                }
            });
            return;
        }

        try {
            String url = brevoBaseUrl + "/contacts";
            log.info("Calling Brevo API to add email: {} to list: {}", email, newsletterListId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey.trim());

            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("listIds", List.of(newsletterListId));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(url, request, String.class);
            log.info("Brevo API response for {}: status={}", email, response.getStatusCode());
        } catch (RestClientException ex) {
            log.error("Brevo API call failed for email {}: {}", email, ex.getMessage(), ex);
        }

        if (existingSubscriber == null) {
            NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                    .email(email).createdAt(LocalDateTime.now()).unsubscribed(false).build();
            this.newsletterRepository.save(subscriber);
        }

        userRepository.findUserByEmail(email).ifPresent(user -> {
            user.setNewsletterSubscription(true);
            userRepository.save(user);
        });
    }

}
