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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.NewsletterSubscriber;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.NewsletterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsletterService {
    private final NewsletterRepository newsletterRepository;
    @Value("${brevo.api-key}")
    private String brevoApiKey;
    @Value("${brevo.newsletter-list-id}")
    private Integer newsletterListId;
    @Value("${brevo.base-url}")
    private final String brevoBaseUrl = "https://api.brevo.com/v3";

    public void addEmail(String email){
        // TODO add some validation this is actually an email
        NewsletterSubscriber existingSubscriber = this.newsletterRepository.findByEmail(email);
        if (existingSubscriber != null && !existingSubscriber.isUnsubscribed()){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email is already subscribed"
            );
        }
        String url = brevoBaseUrl + "/contacts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        Map<String, Object> body = new HashMap<>();

        body.put("email", email);
        body.put("listIds", List.of(newsletterListId));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            var restTemplate = new RestTemplate();
            restTemplate.postForEntity(url, request, String.class);

            NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                    .email(email).createdAt(LocalDateTime.now()).unsubscribed(false).build();
            this.newsletterRepository.save(subscriber);
        } catch (RestClientException ex) {
            System.err.println("Failed to subscribe a new email: " + ex.getMessage());
            throw ex;
        }

    }
    
} 
