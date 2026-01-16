package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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
        NewsletterSubscriber existingSubscriber = this.newsletterRepository.findByEmail(email);
        if (existingSubscriber != null && !existingSubscriber.isUnsubscribed()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already subscribed");
        }

        String url = brevoBaseUrl + "/contacts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey.trim());

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("listIds", List.of(newsletterListId));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        boolean brevoSuccess = false;
        try {
            restTemplate.postForEntity(url, request, String.class);
            brevoSuccess = true;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST &&
                    ex.getResponseBodyAsString().contains("duplicate_parameter")) {
                System.out.println("Email already exists in Brevo, treating as success: " + email);
                brevoSuccess = true;
            } else {
                System.err.println("Brevo API error: " + ex.getMessage());
                throw ex;
            }
        } catch (RestClientException ex) {
            System.err.println("Failed to subscribe email: " + ex.getMessage());
            throw ex;
        }

        if (brevoSuccess) {
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

}
