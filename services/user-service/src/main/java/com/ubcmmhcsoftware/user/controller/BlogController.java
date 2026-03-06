package com.ubcmmhcsoftware.user.controller;

import com.ubcmmhcsoftware.user.dto.BlogPermissionsDTO;
import com.ubcmmhcsoftware.user.enums.BlogPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for blog permissions.
 * Requires ROLE_BLOG_EDITOR, ROLE_BLOG_MANAGER, ROLE_ADMIN, or ROLE_SUPERADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog")
public class BlogController {

    private static final String X_USER_EMAIL = "X-User-Email";
    private static final String X_USER_ROLES = "X-User-Roles";

    @GetMapping
    public ResponseEntity<BlogPermissionsDTO> getUserPermissions(
            @RequestHeader(X_USER_EMAIL) String email,
            @RequestHeader(X_USER_ROLES) String rolesHeader) {

        List<String> roles = parseRoles(rolesHeader);

        return ResponseEntity.ok(BlogPermissionsDTO.builder()
                .user(email)
                .roles(roles)
                .canCreate(BlogPermission.CREATE.isGrantedByAny(roles))
                .canEdit(BlogPermission.EDIT.isGrantedByAny(roles))
                .canDelete(BlogPermission.DELETE.isGrantedByAny(roles))
                .build());
    }

    private static List<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
