package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private AuthService authService;

    private LoginDTO loginDTO;
    private Role userRole;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        userRole = new Role(RoleEnum.ROLE_USER);
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("Should successfully register a new user")
    void registerUser_NewUser_ShouldSaveUser() {
        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(loginDTO.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleEnum.ROLE_USER)).thenReturn(Optional.of(userRole));

        authService.registerUser(loginDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(loginDTO.getEmail(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void registerUser_ExistingEmail_ShouldThrowException() {
        User existingUser = User.builder()
                .email(loginDTO.getEmail())
                .password("existingPassword")
                .build();

        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerUser(loginDTO);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when existing user has Google account")
    void registerUser_GoogleAccount_ShouldThrowException() {
        User googleUser = User.builder()
                .email(loginDTO.getEmail())
                .googleId("google123")
                .build();

        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.of(googleUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerUser(loginDTO);
        });

        assertTrue(exception.getMessage().contains("Google"));
    }

    // ==================== Login Tests ====================

    @Test
    @DisplayName("Should return CustomUserDetails on successful login")
    void loginUser_ValidCredentials_ShouldReturnUserDetails() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .email(loginDTO.getEmail())
                .password("encodedPassword")
                .user_roles(Set.of(userRole))
                .build();

        CustomUserDetails expectedUserDetails = new CustomUserDetails(testUser);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(expectedUserDetails);

        CustomUserDetails result = authService.loginUser(loginDTO);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    // ==================== Forgot Password Tests ====================

    @Test
    @DisplayName("Should create verification token and send email on forgot password")
    void forgotPassword_ValidEmail_ShouldSendEmail() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .email(loginDTO.getEmail())
                .build();

        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(verificationTokenRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(appProperties.getFrontendUrl()).thenReturn("http://localhost:3000");

        authService.forgotPassword(loginDTO.getEmail());

        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendPasswordResetEmail(eq(loginDTO.getEmail()), anyString(), anyString());
    }

    @Test
    @DisplayName("Should silently return when email not found (security)")
    void forgotPassword_InvalidEmail_ShouldReturnSilently() {
        when(userRepository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Should not throw exception - security best practice
        authService.forgotPassword("nonexistent@example.com");

        verify(verificationTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should update existing verification token")
    void forgotPassword_ExistingToken_ShouldUpdateToken() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .email(loginDTO.getEmail())
                .build();

        VerificationToken existingToken = new VerificationToken("oldToken", testUser, 60);

        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(verificationTokenRepository.findByUser(testUser)).thenReturn(Optional.of(existingToken));
        when(appProperties.getFrontendUrl()).thenReturn("http://localhost:3000");

        authService.forgotPassword(loginDTO.getEmail());

        verify(verificationTokenRepository).save(existingToken);
        assertNotEquals("oldToken", existingToken.getToken());
    }

    // ==================== Reset Password Tests ====================

    @Test
    @DisplayName("Should reset password with valid token")
    void resetPassword_ValidToken_ShouldUpdatePassword() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .email(loginDTO.getEmail())
                .password("oldPassword")
                .build();

        VerificationToken validToken = new VerificationToken("123456", testUser, 60);

        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setToken("123456");
        resetDTO.setNewPassword("newPassword123");

        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(validToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        authService.resetPassword(resetDTO);

        assertEquals("encodedNewPassword", testUser.getPassword());
        verify(verificationTokenRepository).delete(validToken);
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void resetPassword_InvalidToken_ShouldThrowException() {
        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setToken("invalid");
        resetDTO.setNewPassword("newPassword123");

        when(verificationTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> {
            authService.resetPassword(resetDTO);
        });
    }

    @Test
    @DisplayName("Should throw exception for expired token")
    void resetPassword_ExpiredToken_ShouldThrowException() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .email(loginDTO.getEmail())
                .build();

        // Create manually expired token
        VerificationToken expiredToken = new VerificationToken("123456", testUser, 60);
        // Use reflection or setter to set expired date
        expiredToken.updateToken("123456", -60); // Expired 60 minutes ago

        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setToken("123456");
        resetDTO.setNewPassword("newPassword123");

        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(expiredToken));

        assertThrows(InvalidTokenException.class, () -> {
            authService.resetPassword(resetDTO);
        });

        verify(verificationTokenRepository).delete(expiredToken);
    }
}
