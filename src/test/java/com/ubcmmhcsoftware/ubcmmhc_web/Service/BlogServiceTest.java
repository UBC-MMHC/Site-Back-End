package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.DTO.BlogPermissionsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @InjectMocks
    private BlogService blogService;

    private UserDetails userWithRoles(String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new User("testuser@example.com", "", authorities);
    }

    @Test
    @DisplayName("Blog editor can create and edit, but not delete")
    void blogEditor_ShouldHaveCreateAndEditOnly() {
        BlogPermissionsDTO dto = blogService.getUserPermissions(
                userWithRoles("ROLE_BLOG_EDITOR"));

        assertTrue(dto.isCanCreate());
        assertTrue(dto.isCanEdit());
        assertFalse(dto.isCanDelete());
        assertEquals("testuser@example.com", dto.getUser());
    }

    @Test
    @DisplayName("Blog manager can create, edit, and delete")
    void blogManager_ShouldHaveAllPermissions() {
        BlogPermissionsDTO dto = blogService.getUserPermissions(
                userWithRoles("ROLE_BLOG_MANAGER"));

        assertTrue(dto.isCanCreate());
        assertTrue(dto.isCanEdit());
        assertTrue(dto.isCanDelete());
    }

    @Test
    @DisplayName("Admin can create, edit, and delete")
    void admin_ShouldHaveAllPermissions() {
        BlogPermissionsDTO dto = blogService.getUserPermissions(
                userWithRoles("ROLE_ADMIN"));

        assertTrue(dto.isCanCreate());
        assertTrue(dto.isCanEdit());
        assertTrue(dto.isCanDelete());
    }

    @Test
    @DisplayName("Superadmin can create, edit, and delete")
    void superadmin_ShouldHaveAllPermissions() {
        BlogPermissionsDTO dto = blogService.getUserPermissions(
                userWithRoles("ROLE_SUPERADMIN"));

        assertTrue(dto.isCanCreate());
        assertTrue(dto.isCanEdit());
        assertTrue(dto.isCanDelete());
    }

    @Test
    @DisplayName("Regular user has no blog permissions")
    void regularUser_ShouldHaveNoPermissions() {
        BlogPermissionsDTO dto = blogService.getUserPermissions(
                userWithRoles("ROLE_USER"));

        assertFalse(dto.isCanCreate());
        assertFalse(dto.isCanEdit());
        assertFalse(dto.isCanDelete());
    }

    @Test
    @DisplayName("Response includes username and roles")
    void response_ShouldIncludeUserAndRoles() {
        BlogPermissionsDTO dto = blogService.getUserPermissions(
                userWithRoles("ROLE_USER", "ROLE_BLOG_EDITOR"));

        assertEquals("testuser@example.com", dto.getUser());
        assertEquals(2, dto.getRoles().size());
        assertTrue(dto.getRoles().containsAll(List.of("ROLE_USER", "ROLE_BLOG_EDITOR")));
    }
}
