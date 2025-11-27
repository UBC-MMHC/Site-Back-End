package com.ubcmmhcsoftware.ubcmmhc_web.Service;

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

/**
 * Service responsible for handling outgoing email communications via the Brevo API.
 * <p>
 * This service utilizes Spring's {@link RestClient} to dispatch HTML-formatted emails via HTTP requests.
 * It is designed to run asynchronously to prevent blocking the main execution thread
 * during the API network call.
 * </p>
 */
@Service
@Slf4j
public class EmailService {

    private final RestClient restClient;
    private final String senderEmail;

    /**
     * Constructor initializes the RestClient with the Brevo API Base URL and Key.
     */
    public EmailService(@Value("${brevo.api-key}") String apiKey,
                        @Value("${spring.mail.sender_email}") String senderEmail) {
        this.senderEmail = senderEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Sends a password reset email containing a dynamic verification link.
     * <p>
     * This method is marked with {@code @Async}.
     * The immediate response is returned to the controller while the email sends in the background.
     * </p>
     *
     * @param to            The recipient's email address.
     * @param subject       The subject line of the email.
     * @param redirect_link The full URL (including token) that the user should click.
     * * Note: Exceptions are caught and logged internally to prevent crashing the async thread.
     */
    @Async
    public void sendPasswordResetEmail(String to, String subject, String redirect_link)  {
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

    /**
     * Helper method to load HTML template files from the classpath resources.
     * <p>
     * Used to keep Java logic separate from HTML styling.
     * </p>
     *
     * @param path The relative path to the file in src/main/resources (e.g., "template/email.html").
     * @return The content of the file as a String.
     * @throws RuntimeException If the file cannot be found or read.
     */
    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template: " + path, e);
        }
    }
}
