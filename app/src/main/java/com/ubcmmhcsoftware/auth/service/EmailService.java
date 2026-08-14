package com.ubcmmhcsoftware.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final RestClient restClient;
    private final String senderEmail;

    public EmailService(@Value("${brevo.api-key}") String apiKey,
                        @Value("${spring.mail.sender_email}") String senderEmail) {
        this.senderEmail = senderEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Async
    public void sendPasswordResetEmail(String to, String subject, String redirect_link) {
        String htmlContent = loadTemplate("template/password_reset_email.html");
        htmlContent = htmlContent.replace("{{reset_link}}", redirect_link);

        Map<String, Object> emailRequest = Map.of(
                "sender", Map.of("name", "MMHC Team", "email", senderEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(emailRequest)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Password reset email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email via Brevo API: {}", e.getMessage());
        }
    }

    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template: " + path, e);
        }
    }
}
