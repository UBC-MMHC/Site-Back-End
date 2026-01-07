package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.CustomUserDetailsService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JWTAuthenticationFilterTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    private UUID testUserId;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();

        testUserId = UUID.randomUUID();
        User testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .password("encodedPassword")
                .user_roles(Set.of(new Role(RoleEnum.ROLE_USER)))
                .build();

        testUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("Should set authentication when valid JWT cookie is present")
    void doFilterInternal_ValidCookie_ShouldSetAuthentication() throws Exception {
        String validToken = "valid.jwt.token";
        Cookie jwtCookie = new Cookie("JWT", validToken);

        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[] { jwtCookie });
        when(appProperties.getJwtCookieName()).thenReturn("JWT");
        when(jwtService.extractId(validToken)).thenReturn(testUserId.toString());
        when(jwtService.isTokenValid(validToken, testUserId.toString())).thenReturn(true);
        when(jwtService.extractRoles(validToken)).thenReturn(List.of("ROLE_USER"));
        when(customUserDetailsService.loadUserById(testUserId)).thenReturn(testUserDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should set authentication when valid Bearer token is present")
    void doFilterInternal_ValidBearerToken_ShouldSetAuthentication() throws Exception {
        String validToken = "valid.jwt.token";

        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractId(validToken)).thenReturn(testUserId.toString());
        when(jwtService.isTokenValid(validToken, testUserId.toString())).thenReturn(true);
        when(jwtService.extractRoles(validToken)).thenReturn(List.of("ROLE_USER"));
        when(customUserDetailsService.loadUserById(testUserId)).thenReturn(testUserDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should continue filter chain without authentication when no token present")
    void doFilterInternal_NoToken_ShouldContinueWithoutAuth() throws Exception {
        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should continue filter chain without authentication when token is invalid")
    void doFilterInternal_InvalidToken_ShouldContinueWithoutAuth() throws Exception {
        String invalidToken = "invalid.jwt.token";
        Cookie jwtCookie = new Cookie("JWT", invalidToken);

        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[] { jwtCookie });
        when(appProperties.getJwtCookieName()).thenReturn("JWT");
        when(jwtService.extractId(invalidToken)).thenReturn(testUserId.toString());
        when(jwtService.isTokenValid(invalidToken, testUserId.toString())).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip filter for excluded paths")
    void shouldNotFilter_ExcludedPath_ShouldReturnTrue() {
        when(request.getServletPath()).thenReturn("/api/newsletter/add-email");

        boolean shouldSkip = jwtAuthenticationFilter.shouldNotFilter(request);

        assertTrue(shouldSkip);
    }

    @Test
    @DisplayName("Should not skip filter for normal paths")
    void shouldNotFilter_NormalPath_ShouldReturnFalse() {
        when(request.getServletPath()).thenReturn("/api/users/profile");

        boolean shouldSkip = jwtAuthenticationFilter.shouldNotFilter(request);

        assertFalse(shouldSkip);
    }

    @Test
    @DisplayName("Should prefer Bearer token over cookie")
    void doFilterInternal_BothTokenPresent_ShouldUseBearerToken() throws Exception {
        String bearerToken = "bearer.jwt.token";
        String cookieToken = "cookie.jwt.token";
        Cookie jwtCookie = new Cookie("JWT", cookieToken);

        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + bearerToken);
        when(jwtService.extractId(bearerToken)).thenReturn(testUserId.toString());
        when(jwtService.isTokenValid(bearerToken, testUserId.toString())).thenReturn(true);
        when(jwtService.extractRoles(bearerToken)).thenReturn(List.of("ROLE_USER"));
        when(customUserDetailsService.loadUserById(testUserId)).thenReturn(testUserDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Should use Bearer token, not cookie token
        verify(jwtService).extractId(bearerToken);
        verify(jwtService, never()).extractId(cookieToken);
    }
}
