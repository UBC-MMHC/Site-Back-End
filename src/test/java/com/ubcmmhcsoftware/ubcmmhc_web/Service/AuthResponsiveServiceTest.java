package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthResponsiveServiceTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthResponsiveService authResponsiveService;

    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .user_roles(Set.of(new Role(RoleEnum.ROLE_USER)))
                .build();

        testUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("Should set JWT cookie with correct properties")
    void handleSuccessfulAuthentication_ShouldSetJwtCookie() throws IOException {
        String testToken = "test.jwt.token";
        when(jwtService.generateToken(testUserDetails)).thenReturn(testToken);
        when(appProperties.isJwtCookieSecure()).thenReturn(true);
        when(appProperties.getJwtCookieSameSite()).thenReturn("None");

        authResponsiveService.handleSuccessfulAuthentication(response, testUserDetails, null);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertTrue(cookieHeader.contains("JWT=" + testToken));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=None"));
    }

    @Test
    @DisplayName("Should redirect when redirect URL is provided")
    void handleSuccessfulAuthentication_WithRedirect_ShouldRedirect() throws IOException {
        String testToken = "test.jwt.token";
        String redirectUrl = "http://localhost:3000/dashboard";

        when(jwtService.generateToken(testUserDetails)).thenReturn(testToken);
        when(appProperties.isJwtCookieSecure()).thenReturn(true);
        when(appProperties.getJwtCookieSameSite()).thenReturn("None");

        authResponsiveService.handleSuccessfulAuthentication(response, testUserDetails, redirectUrl);

        verify(response).sendRedirect(redirectUrl);
    }

    @Test
    @DisplayName("Should not redirect when redirect URL is null")
    void handleSuccessfulAuthentication_NoRedirect_ShouldNotRedirect() throws IOException {
        String testToken = "test.jwt.token";

        when(jwtService.generateToken(testUserDetails)).thenReturn(testToken);
        when(appProperties.isJwtCookieSecure()).thenReturn(true);
        when(appProperties.getJwtCookieSameSite()).thenReturn("None");

        authResponsiveService.handleSuccessfulAuthentication(response, testUserDetails, null);

        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Should not redirect when redirect URL is empty")
    void handleSuccessfulAuthentication_EmptyRedirect_ShouldNotRedirect() throws IOException {
        String testToken = "test.jwt.token";

        when(jwtService.generateToken(testUserDetails)).thenReturn(testToken);
        when(appProperties.isJwtCookieSecure()).thenReturn(true);
        when(appProperties.getJwtCookieSameSite()).thenReturn("None");

        authResponsiveService.handleSuccessfulAuthentication(response, testUserDetails, "");

        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Should respect cookie secure setting from AppProperties")
    void handleSuccessfulAuthentication_InsecureCookie_ShouldNotHaveSecureFlag() throws IOException {
        String testToken = "test.jwt.token";
        when(jwtService.generateToken(testUserDetails)).thenReturn(testToken);
        when(appProperties.isJwtCookieSecure()).thenReturn(false);
        when(appProperties.getJwtCookieSameSite()).thenReturn("Lax");

        authResponsiveService.handleSuccessfulAuthentication(response, testUserDetails, null);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertFalse(cookieHeader.contains("Secure"));
        assertTrue(cookieHeader.contains("SameSite=Lax"));
    }
}
