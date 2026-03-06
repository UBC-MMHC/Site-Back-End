package com.ubcmmhcsoftware.auth.service;

import com.ubcmmhcsoftware.auth.config.AppProperties;
import com.ubcmmhcsoftware.auth.config.CustomUserDetails;
import com.ubcmmhcsoftware.auth.dto.LoginDTO;
import com.ubcmmhcsoftware.auth.dto.ResetPasswordDTO;
import com.ubcmmhcsoftware.auth.entity.Role;
import com.ubcmmhcsoftware.auth.entity.User;
import com.ubcmmhcsoftware.auth.entity.VerificationToken;
import com.ubcmmhcsoftware.auth.enums.RoleEnum;
import com.ubcmmhcsoftware.auth.exception.InvalidTokenException;
import com.ubcmmhcsoftware.auth.exception.UserAlreadyExistsException;
import com.ubcmmhcsoftware.auth.repository.RoleRepository;
import com.ubcmmhcsoftware.auth.repository.UserRepository;
import com.ubcmmhcsoftware.auth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

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
    private final AppProperties appProperties;

    public void registerUser(LoginDTO loginDTO) {
        Optional<User> userExists = userRepository.findUserByEmail(loginDTO.getEmail());

        if (userExists.isPresent()) {
            User existingUser = userExists.get();

            if (existingUser.getPassword() != null && !existingUser.getPassword().isEmpty()) {
                throw new UserAlreadyExistsException("User with email already exists");
            }

            if (existingUser.getGoogleId() != null) {
                throw new UserAlreadyExistsException(
                        "Account exists via Google. Please login with Google to access your account.");
            }
        }

        User user = new User();
        user.setEmail(loginDTO.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));

        Role role = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setName(RoleEnum.ROLE_USER);
        user.setUser_roles(Set.of(role));

        userRepository.save(user);
    }

    public CustomUserDetails loginUser(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        return (CustomUserDetails) authentication.getPrincipal();
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if (user == null)
            return;

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

        String link = String.format("%s/reset-password?token=%s", appProperties.getFrontendUrl(),
                URLEncoder.encode(token, StandardCharsets.UTF_8));
        emailService.sendPasswordResetEmail(user.getEmail(), "Your Password Reset Link", link);
    }

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

    private String generateVerificationToken() {
        return new DecimalFormat("000000").format(new Random().nextInt(999999));
    }
}
