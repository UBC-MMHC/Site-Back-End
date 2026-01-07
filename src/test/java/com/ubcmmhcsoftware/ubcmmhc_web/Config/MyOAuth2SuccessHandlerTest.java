package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.CustomUserDetailsService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyOAuth2SuccessHandlerTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private JWTService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private MyOAuth2SuccessHandler oAuth2SuccessHandler;

    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .email("oauth@example.com")
                .user_roles(Set.of(new Role(RoleEnum.ROLE_USER)))
                .build();

        testUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("Should generate JWT and redirect after successful OAuth2 authentication")
    void onAuthenticationSuccess_ShouldGenerateTokenAndRedirect() throws Exception {
        String email = "oauth@example.com";
        String jwtToken = "generated.jwt.token";
        String frontendCallback = "http://localhost:3000/auth/callback";

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(testUserDetails);
        when(jwtService.generateToken(testUserDetails)).thenReturn(jwtToken);
        when(appProperties.getRedirectAfterLogin()).thenReturn(frontendCallback);

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.startsWith(frontendCallback));
        assertTrue(redirectUrl.contains("token=" + jwtToken));
    }

    @Test
    @DisplayName("Should load user by email from OAuth2 principal")
    void onAuthenticationSuccess_ShouldLoadUserByEmail() throws Exception {
        String email = "oauth@example.com";

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(testUserDetails);
        when(jwtService.generateToken(testUserDetails)).thenReturn("token");
        when(appProperties.getRedirectAfterLogin()).thenReturn("http://localhost:3000/auth/callback");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(customUserDetailsService).loadUserByUsername(email);
    }

    @Test
    @DisplayName("Should generate token with correct user details")
    void onAuthenticationSuccess_ShouldGenerateTokenWithCorrectUser() throws Exception {
        String email = "oauth@example.com";

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(testUserDetails);
        when(jwtService.generateToken(testUserDetails)).thenReturn("token");
        when(appProperties.getRedirectAfterLogin()).thenReturn("http://localhost:3000/auth/callback");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(jwtService).generateToken(testUserDetails);
    }
}
