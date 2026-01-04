package com.ubcmmhcsoftware.ubcmmhc_web.ServiceTest;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomeUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setUser_roles(Set.of(userRole));
    }

    // ==================== loadUserByUsername Tests ====================

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        CustomUserDetails result = customUserDetailsService.loadUserByUsername("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@example.com");
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getAuthorities()).hasSize(1);
    }

    @Test
    void loadUserByUsername_ThrowsWhenNotFound() {
        when(userRepository.findUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("nonexistent@example.com");
    }

    // ==================== loadUserById Tests ====================

    @Test
    void loadUserById_Success() {
        when(userRepository.findUserByIdWithRoles(userId)).thenReturn(Optional.of(user));

        CustomUserDetails result = customUserDetailsService.loadUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void loadUserById_ThrowsWhenNotFound() {
        UUID nonexistentId = UUID.randomUUID();
        when(userRepository.findUserByIdWithRoles(nonexistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserById(nonexistentId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(nonexistentId.toString());
    }

    @Test
    void loadUserById_ReturnsUserWithMultipleRoles() {
        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ROLE_ADMIN);

        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_USER);

        user.setUser_roles(Set.of(userRole, adminRole));

        when(userRepository.findUserByIdWithRoles(userId)).thenReturn(Optional.of(user));

        CustomUserDetails result = customUserDetailsService.loadUserById(userId);

        assertThat(result.getAuthorities()).hasSize(2);
    }
}
