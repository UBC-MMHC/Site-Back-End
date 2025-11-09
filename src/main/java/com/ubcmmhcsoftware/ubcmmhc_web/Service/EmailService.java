package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // TODO Make so login url is a button instead of url
    public void sendEmail(String to, String subject, String text) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("ubcmmhc@gmail.com", "MMHC Team");
        helper.setTo(to);
        helper.setSubject(subject);

        String htmlContent =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "  <meta charset='UTF-8'/>" +
                        "  <meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
                        "  <style>" +
                        "    .container {" +
                        "      max-width: 480px;" +
                        "      margin: 20px auto;" +
                        "      padding: 20px;" +
                        "      background: #ffffff;" +
                        "      border-radius: 12px;" +
                        "      font-family: Arial, sans-serif;" +
                        "      box-shadow: 0 4px 12px rgba(0,0,0,0.08);" +
                        "      text-align: center;" +
                        "    }" +
                        "    .title {" +
                        "      font-size: 22px;" +
                        "      font-weight: bold;" +
                        "      color: #16345A;" +
                        "      margin-bottom: 10px;" +
                        "    }" +
                        "    .text {" +
                        "      font-size: 16px;" +
                        "      color: #444;" +
                        "      margin-bottom: 24px;" +
                        "    }" +
                        "    .button {" +
                        "      display: inline-block;" +
                        "      padding: 12px 22px;" +
                        "      font-size: 16px;" +
                        "      background-color: #16345A;" +
                        "      color: white !important;" +
                        "      text-decoration: none;" +
                        "      border-radius: 6px;" +
                        "      font-weight: bold;" +
                        "    }" +
                        "    .footer {" +
                        "      margin-top: 25px;" +
                        "      font-size: 12px;" +
                        "      color: #888;" +
                        "    }" +
                        "  </style>" +
                        "</head>" +
                        "<body style='background:#f4f4f4; padding:20px;'>" +
                        "  <div class='container'>" +
                        "    <div class='title'>Login to MMHC</div>" +
                        "    <div class='text'>Click the button below to securely log in to your account:</div>" +
                        "    <a href='" + text + "' class='button'>Log In</a>" +
                        "    <div class='footer'>If you didn’t request this email, you can safely ignore it.</div>" +
                        "  </div>" +
                        "</body>" +
                        "</html>";

        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }
}
