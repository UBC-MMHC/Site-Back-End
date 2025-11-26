package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.URLConstant;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.ResetPasswordDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.VerificationToken;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Exception.InvalidTokenException;
import com.ubcmmhcsoftware.ubcmmhc_web.Exception.UserAlreadyExistsException;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.RoleRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Service responsible for handling user authentication, registration,
 * and password management logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final int TOKEN_EXPIRATION_TIME = 60;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;

    /**
     * Registers a new user based on the provided login credentials.
     * <p>
     * Checks if the user already exists via email. If the user exists with a Google ID,
     * throws an exception instructing them to use Google Login.
     * </p>
     *
     * @param loginDTO Data transfer object containing the email and password.
     * @throws UserAlreadyExistsException If a user with the given email already exists.
     */
    public void registerUser(LoginDTO loginDTO) {
        Optional<User> useExists = userRepository.findUserByEmail(loginDTO.getEmail());

        if (useExists.isPresent()) {
            User existingUser = useExists.get();

            if (existingUser.getPassword() != null && !existingUser.getPassword().isEmpty()) {
                throw new UserAlreadyExistsException("User with email already exists");
            }

            if (existingUser.getGoogleId() != null) {
                throw new UserAlreadyExistsException("Account exists via Google. Please login with Google to access your account.");
            }
        }

        User user = new User();
        user.setEmail(loginDTO.getEmail());
        user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));

        Role role = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setName(RoleEnum.ROLE_USER);
        user.setUser_roles(Set.of(role));

        userRepository.save(user);
    }

    /**
     * Authenticates a user using Spring Security's AuthenticationManager.
     *
     * @param loginDTO Data transfer object containing the email and password.
     * @return CustomUserDetails containing the authenticated user's principal data.
     */
    public CustomUserDetails loginUser(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        return (CustomUserDetails) authentication.getPrincipal();
    }

    /**
     * Initiates the forgot password process.
     * <p>
     * Generates a 6-digit verification token, saves it to the database, and sends
     * an email with a link to the frontend reset page.
     * </p>
     * inputs the new password, and calls the resetPassword endpoint.
     *
     * @param email The email address of the user requesting the password reset.
     * @throws MessagingException           If sending the email fails.
     * @throws UnsupportedEncodingException If URL encoding of the token fails.
     */
    @Transactional
    public void forgotPassword(String email) throws MessagingException, UnsupportedEncodingException {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if (user == null) return;

        String token = generateVerificationToken();

        Optional<VerificationToken> existingToken = verificationTokenRepository.findByUser(user);

        if (existingToken.isPresent()) {
            VerificationToken vt = existingToken.get();
            vt.setToken(token);
            vt.updateToken(token, TOKEN_EXPIRATION_TIME);
            verificationTokenRepository.save(vt);
        } else {
            VerificationToken newToken = new VerificationToken(token, user, TOKEN_EXPIRATION_TIME);
            verificationTokenRepository.save(newToken);
        }

        String link = String.format("%s/reset-password?token=%s", URLConstant.FRONTEND_URL, URLEncoder.encode(token, StandardCharsets.UTF_8));
        emailService.sendPasswordResetEmail(user.getEmail(), "Your Password Reset Link", link);
    }

    /**
     * Resets the user's password if the provided token is valid and not expired.
     *
     * @param resetPasswordDTO Data transfer object containing the 6-digit token and new password.
     * @throws RuntimeException If the token is invalid or expired.
     */
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(resetPasswordDTO.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Token has expired");
        }

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setVerificationToken(null);

        verificationTokenRepository.delete(verificationToken);
    }

    /*
     * The following methods (requestLoginCode, verifyLoginCode) are currently disabled.
     * Un-comment if implementing Magic Link / One-Time-Password login logic.
     */

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
        return new DecimalFormat("000000").format(new Random().nextInt(999999));
    }


}
