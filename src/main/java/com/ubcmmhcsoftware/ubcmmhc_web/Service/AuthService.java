package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.URLConstant;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.VerificationDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.VerificationToken;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.VerificationTokenRepository;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ott.InvalidOneTimeTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    // Time for token expiration 10 * 60 seconds = 6 minutes
    private final int TOKEN_EXPIRATION_TIME = 60;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final CustomUserDetailsService customUserDetailsService;

    // upon login we check if user is registered if no, register them, then we generate token and email it
    public void requestLoginCode(LoginDTO loginDTO) throws MessagingException, UnsupportedEncodingException {
        User user = userRepository.findUserByEmail(loginDTO.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(loginDTO.getEmail());
                    return userRepository.save(newUser);
                });

        String token = generateVerificationToken();

        VerificationToken verificationToken = new VerificationToken(token, user, TOKEN_EXPIRATION_TIME);
        verificationTokenRepository.save(verificationToken);

        String verificationUrl = URLConstant.BACKEND_URL + "/api/auth/verify-token";
        String link = String.format("%s?email=%s&token=%s",
                verificationUrl,
                URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8),
                URLEncoder.encode(token, StandardCharsets.UTF_8));

//        String email = "Click this link to login: \n \n" + link;

        emailService.sendEmail(loginDTO.getEmail(), "Your Login Code", link );
    }


    // Verifies verification code
    @Transactional
    public CustomUserDetails verifyLoginCode(String email, String received_token) throws AuthenticationFailedException {
        Optional<VerificationToken> token = verificationTokenRepository.findByToken(received_token);

        if (token.isEmpty()) {
            throw new InvalidOneTimeTokenException("Invalid token");
        }

        if (Instant.now().isAfter(token.get().getExpiryDate())) {
            verificationTokenRepository.deleteById(token.get().getId());
            throw new InvalidOneTimeTokenException("Token is expired");
        }

        if (!token.get().getUser().getEmail().equals(email))
            throw new AuthenticationFailedException("Email not valid");

        CustomUserDetails user = customUserDetailsService.loadUserByUsername(token.get().getUser().getEmail());
        verificationTokenRepository.deleteByUser_Email(email);
        return user;
    }

    // Creates 6 digit code
    private String generateVerificationToken() {
        return new DecimalFormat("000000")
                .format(new Random().nextInt(999999));
    }


}
