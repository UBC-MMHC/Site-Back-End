package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .password("encodedPassword")
                .user_roles(Set.of(new Role(RoleEnum.ROLE_USER)))
                .build();
    }

    // ==================== Load By Username (Email) Tests ====================

    @Test
    @DisplayName("Should load user by email successfully")
    void loadUserByUsername_ValidEmail_ShouldReturnUserDetails() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        CustomUserDetails result = customUserDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("test@example.com", result.getUsername());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException for unknown email")
    void loadUserByUsername_InvalidEmail_ShouldThrowException() {
        when(userRepository.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("unknown@example.com");
        });

        assertEquals("unknown@example.com", exception.getMessage());
    }

    @Test
    @DisplayName("Should return user with correct authorities")
    void loadUserByUsername_ShouldReturnCorrectAuthorities() {
        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .password("encodedPassword")
                .user_roles(Set.of(
                        new Role(RoleEnum.ROLE_USER),
                        new Role(RoleEnum.ROLE_ADMIN)))
                .build();

        when(userRepository.findUserByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        CustomUserDetails result = customUserDetailsService.loadUserByUsername("admin@example.com");

        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    // ==================== Load By ID Tests ====================

    @Test
    @DisplayName("Should load user by ID successfully")
    void loadUserById_ValidId_ShouldReturnUserDetails() {
        when(userRepository.findUserByIdWithRoles(testUserId)).thenReturn(Optional.of(testUser));

        CustomUserDetails result = customUserDetailsService.loadUserById(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException for unknown ID")
    void loadUserById_InvalidId_ShouldThrowException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findUserByIdWithRoles(unknownId)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserById(unknownId);
        });

        assertEquals(unknownId.toString(), exception.getMessage());
    }
}
