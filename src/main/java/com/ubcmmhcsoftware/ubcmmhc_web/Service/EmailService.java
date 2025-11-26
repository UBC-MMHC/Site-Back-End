package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Service responsible for handling outgoing email communications.
 * <p>
 * This service utilizes Spring's JavaMailSender to dispatch HTML-formatted emails.
 * It is designed to run asynchronously to prevent blocking the main execution thread
 * during email transmission.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.sender_email}")
    private String senderEmail;

    /**
     * Sends a password reset email containing a dynamic verification link.
     * <p>
     * This method is marked with {@code @Async}, meaning it runs on a separate thread.
     * The immediate response is returned to the controller while the email sends in the background,
     * ensuring a fast user experience.
     * </p>
     *
     * @param to            The recipient's email address.
     * @param subject       The subject line of the email.
     * @param redirect_link The full URL (including token) that the user should click.
     * @throws MessagingException           If the email server rejects the message or connection fails.
     * @throws UnsupportedEncodingException If the sender name encoding is invalid.
     */
    @Async
    public void sendPasswordResetEmail(String to, String subject, String redirect_link) throws MessagingException, UnsupportedEncodingException {
        String htmlContent = loadTemplate("template/password_reset_email.html");
        htmlContent = htmlContent.replace("{{reset_link}}", redirect_link);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(senderEmail, "MMHC Team");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // tr®// ue = HTML

        mailSender.send(message);
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
