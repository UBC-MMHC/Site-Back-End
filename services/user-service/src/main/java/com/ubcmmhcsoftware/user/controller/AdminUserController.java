package com.ubcmmhcsoftware.user.controller;

import com.ubcmmhcsoftware.user.entity.Role;
import com.ubcmmhcsoftware.user.entity.RoleEnum;
import com.ubcmmhcsoftware.user.entity.User;
import com.ubcmmhcsoftware.user.repository.RoleRepository;
import com.ubcmmhcsoftware.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for admin user operations (role assignment).
 * All endpoints require ROLE_ADMIN. Gateway routes /api/admin/users/** here.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Slf4j
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Gets the highest role level for the current admin from the database.
     * This prevents privilege escalation by ensuring admins can only manage roles
     * at or below their level.
     */
    private int getAdminRoleLevel(String adminEmail) {
        return userRepository.findUserByEmailWithRoles(adminEmail)
                .map(admin -> admin.getUser_roles().stream()
                        .mapToInt(role -> role.getName().getLevel())
                        .max()
                        .orElse(0))
                .orElse(0);
    }

    /**
     * Assigns a role to a user.
     */
    @PostMapping("/{userEmail}/role")
    public ResponseEntity<?> assignRole(
            @PathVariable String userEmail,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails adminDetails) {

        if (adminDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String roleStr = requestBody.get("role");
        if (roleStr == null || roleStr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "role is required (ROLE_USER, ROLE_BLOG_EDITOR, ROLE_BLOG_MANAGER, ROLE_ADMIN, or ROLE_SUPERADMIN)"));
        }

        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role. Must be ROLE_USER, ROLE_BLOG_EDITOR, ROLE_BLOG_MANAGER, ROLE_ADMIN, or ROLE_SUPERADMIN"));
        }

        User user = userRepository.findUserByEmailWithRoles(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found: " + userEmail));
        }

        Role role = roleRepository.findByName(roleEnum).orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Role not found in database"));
        }

        if (user.getUser_roles().contains(role)) {
            return ResponseEntity.ok(Map.of("message", "User already has this role"));
        }

        // Hierarchy check: admin can only assign roles at or below their level
        int adminLevel = getAdminRoleLevel(adminDetails.getUsername());
        if (roleEnum.getLevel() > adminLevel) {
            log.warn("Admin {} (level {}) attempted to assign higher role {} (level {}) to {}",
                    adminDetails.getUsername(), adminLevel, roleEnum, roleEnum.getLevel(), userEmail);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Cannot assign a role higher than your own"));
        }

        user.getUser_roles().add(role);
        userRepository.save(user);
        log.info("Admin {} assigned role {} to user {}", adminDetails.getUsername(), roleEnum, userEmail);

        return ResponseEntity.ok(Map.of(
                "message", "Role assigned successfully",
                "userEmail", userEmail,
                "role", roleEnum.name()));
    }

    /**
     * Removes a role from a user.
     */
    @DeleteMapping("/{userEmail}/role")
    public ResponseEntity<?> removeRole(
            @PathVariable String userEmail,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails adminDetails) {

        if (adminDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String roleStr = requestBody.get("role");
        if (roleStr == null || roleStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "role is required"));
        }

        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }

        User user = userRepository.findUserByEmailWithRoles(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        Role role = roleRepository.findByName(roleEnum).orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Role not found"));
        }

        // Hierarchy check: admin can only remove roles at or below their level
        int adminLevel = getAdminRoleLevel(adminDetails.getUsername());
        if (roleEnum.getLevel() > adminLevel) {
            log.warn("Admin {} (level {}) attempted to remove higher role {} (level {}) from {}",
                    adminDetails.getUsername(), adminLevel, roleEnum, roleEnum.getLevel(), userEmail);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Cannot remove a role higher than your own"));
        }

        user.getUser_roles().remove(role);
        userRepository.save(user);
        log.info("Admin {} removed role {} from user {}", adminDetails.getUsername(), roleEnum, userEmail);

        return ResponseEntity.ok(Map.of(
                "message", "Role removed successfully",
                "userEmail", userEmail,
                "role", roleEnum.name()));
    }
}
