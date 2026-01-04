package com.ubcmmhcsoftware.ubcmmhc_web.ServiceTest;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JWTServiceTest {

    private JWTService jwtService;
    private CustomUserDetails userDetails;
    private User user;
    private String secret;

    @BeforeEach
    void setUp() {
        // Secret must be at least 256 bits (32 characters) for HS256
        secret = "ThisIsAVerySecureSecretKeyForTesting123456";
        jwtService = new JWTService(secret);

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
    void generateToken_ContainsCorrectSubject() {
        String token = jwtService.generateToken(userDetails);

        String extractedId = jwtService.extractId(token);

        assertThat(extractedId).isEqualTo(user.getId().toString());
    }

    @Test
    void generateToken_ContainsRoles() {
        String token = jwtService.generateToken(userDetails);

        List<String> roles = jwtService.extractRoles(token);

        assertThat(roles).contains("ROLE_USER");
    }

    @Test
    void generateToken_ContainsMultipleRoles() {
        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ROLE_ADMIN);

        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);

        user.setUser_roles(Set.of(userRole, adminRole));
        CustomUserDetails multiRoleUserDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(multiRoleUserDetails);
        List<String> roles = jwtService.extractRoles(token);

        assertThat(roles).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void extractId_ReturnsCorrectUserId() {
        String token = jwtService.generateToken(userDetails);

        String extractedId = jwtService.extractId(token);

        assertThat(extractedId).isEqualTo(user.getId().toString());
    }

    @Test
    void extractRoles_ReturnsCorrectRoles() {
        String token = jwtService.generateToken(userDetails);

        List<String> roles = jwtService.extractRoles(token);

        assertThat(roles).isNotEmpty();
        assertThat(roles).contains("ROLE_USER");
    }

    @Test
    void isTokenValid_ReturnsTrueForValidToken() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, user.getId().toString());

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ReturnsFalseForWrongUser() {
        String token = jwtService.generateToken(userDetails);
        String differentUserId = UUID.randomUUID().toString();

        boolean isValid = jwtService.isTokenValid(token, differentUserId);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_ThrowsForExpiredToken() {
        // Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(user.getId().toString())
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // 24 hours ago
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago (expired)
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, user.getId().toString()))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
