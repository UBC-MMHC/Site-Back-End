package com.ubcmmhcsoftware.ubcmmhc_web.ServiceTest;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthResponsiveService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthResponsiveServiceTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthResponsiveService authResponsiveService;

    private CustomUserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setUser_roles(Set.of(userRole));

        userDetails = new CustomUserDetails(user);
    }

    @Test
    void handleSuccessfulAuthentication_SetsJwtCookie() throws IOException {
        String expectedToken = "jwt.token.here";
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        authResponsiveService.handleSuccessfulAuthentication(response, userDetails, null);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookieValue = headerCaptor.getValue();
        assertThat(cookieValue).contains("JWT=" + expectedToken);
        assertThat(cookieValue).contains("HttpOnly");
        assertThat(cookieValue).contains("Secure");
        assertThat(cookieValue).contains("Path=/");
        assertThat(cookieValue).contains("SameSite=None");
    }

    @Test
    void handleSuccessfulAuthentication_WithRedirect_Redirects() throws IOException {
        String expectedToken = "jwt.token.here";
        String redirectUrl = "http://localhost:3000/dashboard";

        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        authResponsiveService.handleSuccessfulAuthentication(response, userDetails, redirectUrl);

        verify(response).sendRedirect(redirectUrl);
    }

    @Test
    void handleSuccessfulAuthentication_WithoutRedirect_NoRedirect() throws IOException {
        String expectedToken = "jwt.token.here";
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        authResponsiveService.handleSuccessfulAuthentication(response, userDetails, null);

        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void handleSuccessfulAuthentication_WithEmptyRedirect_NoRedirect() throws IOException {
        String expectedToken = "jwt.token.here";
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        authResponsiveService.handleSuccessfulAuthentication(response, userDetails, "");

        verify(response, never()).sendRedirect(anyString());
    }
}
