package com.ubcmmhcsoftware.ubcmmhc_web.ServiceTest;

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
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.EmailService;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

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
    private User user;
    private Role userRole;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setUser_roles(Set.of(userRole));
    }

    // ==================== registerUser Tests ====================

    @Test
    void registerUser_Success() {
        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(loginDTO.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleEnum.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerUser(loginDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(loginDTO.getEmail());
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getUser_roles()).contains(userRole);
    }

    @Test
    void registerUser_ThrowsWhenUserExistsWithPassword() {
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("existingPassword");

        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.registerUser(loginDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ThrowsWhenUserExistsWithGoogleId() {
        User googleUser = new User();
        googleUser.setEmail("test@example.com");
        googleUser.setGoogleId("google123");

        when(userRepository.findUserByEmail(loginDTO.getEmail())).thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> authService.registerUser(loginDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Account exists via Google. Please login with Google to access your account.");

        verify(userRepository, never()).save(any());
    }

    // ==================== loginUser Tests ====================

    @Test
    void loginUser_Success() {
        CustomUserDetails expectedUserDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(expectedUserDetails);

        CustomUserDetails result = authService.loginUser(loginDTO);

        assertThat(result).isEqualTo(expectedUserDetails);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // ==================== forgotPassword Tests ====================

    @Test
    void forgotPassword_Success_CreatesNewToken() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(appProperties.getFrontendUrl()).thenReturn("http://localhost:3000");

        authService.forgotPassword(user.getEmail());

        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void forgotPassword_Success_UpdatesExistingToken() {
        VerificationToken existingToken = new VerificationToken("oldToken", user, 60);

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));
        when(appProperties.getFrontendUrl()).thenReturn("http://localhost:3000");

        authService.forgotPassword(user.getEmail());

        verify(verificationTokenRepository).save(existingToken);
        verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void forgotPassword_UserNotFound_NoAction() {
        when(userRepository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword("nonexistent@example.com");

        verify(verificationTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    // ==================== resetPassword Tests ====================

    @Test
    void resetPassword_Success() {
        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setToken("validToken");
        resetDTO.setNewPassword("newPassword123");

        VerificationToken token = new VerificationToken("validToken", user, 60);

        when(verificationTokenRepository.findByToken("validToken")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        authService.resetPassword(resetDTO);

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(user.getVerificationToken()).isNull();
        verify(verificationTokenRepository).delete(token);
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setToken("invalidToken");
        resetDTO.setNewPassword("newPassword123");

        when(verificationTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(resetDTO))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid token");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setToken("expiredToken");
        resetDTO.setNewPassword("newPassword123");

        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setToken("expiredToken");
        expiredToken.setUser(user);
        expiredToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.HOURS));

        when(verificationTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.resetPassword(resetDTO))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token has expired");

        verify(verificationTokenRepository).delete(expiredToken);
        verify(passwordEncoder, never()).encode(anyString());
    }
}
