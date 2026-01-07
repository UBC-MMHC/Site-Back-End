package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JWTServiceTest {

    private static final String TEST_SECRET = "this-is-a-very-secure-secret-key-for-testing-purposes-256-bits";
    private static final long EXPIRATION_SECONDS = 604800L; // 7 days

    @Mock
    private AppProperties appProperties;

    private JWTService jwtService;
    private CustomUserDetails testUserDetails;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        when(appProperties.getJwtExpirationSeconds()).thenReturn(EXPIRATION_SECONDS);
        jwtService = new JWTService(TEST_SECRET, appProperties);

        // Create test user
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
    @DisplayName("Should generate a valid JWT token")
    void generateToken_ShouldReturnValidToken() {
        String token = jwtService.generateToken(testUserDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT format: header.payload.signature
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should extract correct user ID from token")
    void extractId_ShouldReturnCorrectUserId() {
        String token = jwtService.generateToken(testUserDetails);

        String extractedId = jwtService.extractId(token);

        assertEquals(testUserId.toString(), extractedId);
    }

    @Test
    @DisplayName("Should extract correct roles from token")
    void extractRoles_ShouldReturnCorrectRoles() {
        String token = jwtService.generateToken(testUserDetails);

        List<String> roles = jwtService.extractRoles(token);

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    @DisplayName("Should validate token with correct user ID")
    void isTokenValid_WithCorrectId_ShouldReturnTrue() {
        String token = jwtService.generateToken(testUserDetails);

        boolean isValid = jwtService.isTokenValid(token, testUserId.toString());

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate token with incorrect user ID")
    void isTokenValid_WithIncorrectId_ShouldReturnFalse() {
        String token = jwtService.generateToken(testUserDetails);
        String wrongId = UUID.randomUUID().toString();

        boolean isValid = jwtService.isTokenValid(token, wrongId);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should detect expired token")
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        // Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .header().type("JWT").and()
                .subject(testUserId.toString())
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date(System.currentTimeMillis() - 1000000))
                .expiration(new Date(System.currentTimeMillis() - 500000)) // Expired
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThrows(Exception.class, () -> {
            jwtService.isTokenValid(expiredToken, testUserId.toString());
        });
    }

    @Test
    @DisplayName("Should throw exception for malformed token")
    void extractId_WithMalformedToken_ShouldThrowException() {
        String malformedToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            jwtService.extractId(malformedToken);
        });
    }

    @Test
    @DisplayName("Should generate token with multiple roles")
    void generateToken_WithMultipleRoles_ShouldContainAllRoles() {
        // Create user with multiple roles
        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .password("encodedPassword")
                .user_roles(Set.of(
                        new Role(RoleEnum.ROLE_USER),
                        new Role(RoleEnum.ROLE_ADMIN)))
                .build();

        CustomUserDetails adminDetails = new CustomUserDetails(adminUser);
        String token = jwtService.generateToken(adminDetails);

        List<String> roles = jwtService.extractRoles(token);

        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }
}
