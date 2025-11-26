package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.URLConstant;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.VerificationToken;
import com.ubcmmhcsoftware.ubcmmhc_web.Exception.UserAlreadyExistsException;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final int TOKEN_EXPIRATION_TIME = 60;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public void registerUser(LoginDTO loginDTO) {
        Optional<User> useExists = userRepository.findUserByEmail(loginDTO.getEmail());

        if (useExists.isPresent()) {
            User existingUser = useExists.get();

            if (existingUser.getPassword() != null && !existingUser.getPassword().isEmpty()) {
                throw new UserAlreadyExistsException("User with email already exists");
            }

            if (existingUser.getGoogleId() != null) {
                throw new UserAlreadyExistsException(
                        "Account exists via Google. Please login with Google to access your account."
                );
            }
        }

        User user = new User();
        user.setEmail(loginDTO.getEmail());
        user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));

        userRepository.save(user);
    }

    public CustomUserDetails loginUser(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        return (CustomUserDetails) authentication.getPrincipal();

    }
    // TODO Will redirec tto frontend reset password page snd when password inputed and submit cliked call the resetPasswordCall
    @Transactional
    public void forgotPassword(String email) throws MessagingException, UnsupportedEncodingException {
        Optional<User> user = userRepository.findUserByEmail(email);

        if (user.isEmpty()) return;

        String token = generateVerificationToken();

        VerificationToken verificationToken = new VerificationToken(token, user.get(), TOKEN_EXPIRATION_TIME);

        verificationTokenRepository.deleteByUser_Id(user.get().getId());
        verificationTokenRepository.save(verificationToken);

        String link = String.format("%s/reset-password?token=%s",
                URLConstant.FRONTEND_URL,
                URLEncoder.encode(token, StandardCharsets.UTF_8));

        emailService.sendPasswordResetEmail(user.get().getEmail(), "Your Password Reset Link", link);

//        System.out.println("Your Password Reset Link is: " + link);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Verification token invalid"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new RuntimeException("Token has expired");
        }

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }


    // upon login we check if user is registered if no, register them, then we generate token and email it
//    public void requestLoginCode(LoginDTO loginDTO) throws MessagingException, UnsupportedEncodingException {
//        User user = userRepository.findUserByEmail(loginDTO.getEmail())
//                .orElseGet(() -> {
//                    User newUser = new User();
//                    newUser.setEmail(loginDTO.getEmail());
//                    return userRepository.save(newUser);
//                });
//
//        String token = generateVerificationToken();
//
//        VerificationToken verificationToken = new VerificationToken(token, user, TOKEN_EXPIRATION_TIME);
//        verificationTokenRepository.save(verificationToken);
//
//        String verificationUrl = URLConstant.BACKEND_URL + "/api/auth/verify-token";
//        String link = String.format("%s?email=%s&token=%s",
//                verificationUrl,
//                URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8),
//                URLEncoder.encode(token, StandardCharsets.UTF_8));
//

    /// /        String email = "Click this link to login: \n \n" + link;
//
//        emailService.sendEmail(loginDTO.getEmail(), "Your Login Code", link );
//    }


//    // Verifies verification code
//    @Transactional
//    public CustomUserDetails verifyLoginCode(String email, String received_token) throws AuthenticationFailedException {
//        Optional<VerificationToken> token = verificationTokenRepository.findByToken(received_token);
//
//        if (token.isEmpty()) {
//            throw new InvalidOneTimeTokenException("Invalid token");
//        }
//
//        if (Instant.now().isAfter(token.get().getExpiryDate())) {
//            verificationTokenRepository.deleteById(token.get().getId());
//            throw new InvalidOneTimeTokenException("Token is expired");
//        }
//
//        if (!token.get().getUser().getEmail().equals(email))
//            throw new AuthenticationFailedException("Email not valid");
//
//        CustomUserDetails user = customUserDetailsService.loadUserByUsername(token.get().getUser().getEmail());
//        verificationTokenRepository.deleteByUser_Email(email);
//        return user;
//    }

    // Creates 6 digit code
    private String generateVerificationToken() {
        return new DecimalFormat("000000")
                .format(new Random().nextInt(999999));
    }


}
