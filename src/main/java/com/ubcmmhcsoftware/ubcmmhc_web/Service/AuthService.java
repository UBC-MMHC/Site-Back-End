package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Exception.UserAlreadyExistsException;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out successfully");
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
////        String email = "Click this link to login: \n \n" + link;
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

//    // Creates 6 digit code
//    private String generateVerificationToken() {
//        return new DecimalFormat("000000")
//                .format(new Random().nextInt(999999));
//    }


}
