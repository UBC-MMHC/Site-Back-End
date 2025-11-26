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

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.sender_email}")
    private String senderEmail;

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

    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template: " + path, e);
        }
    }
}
