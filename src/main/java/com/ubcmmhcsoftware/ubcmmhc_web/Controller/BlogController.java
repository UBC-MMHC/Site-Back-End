package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for blog operations.
 * Acts as the gateway entry point for blog features.
 *
 * Access requires one of: ROLE_BLOG_EDITOR, ROLE_BLOG_MANAGER, ROLE_ADMIN, or
 * ROLE_SUPERADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog")
@Slf4j
public class BlogController {

    /**
     * Returns the authenticated user's blog-related permissions.
     * what blog actions the user is allowed to perform.
     */
    @GetMapping
    public ResponseEntity<?> getUserPermissions(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        boolean canCreate = roles.stream().anyMatch(r -> r.equals("ROLE_BLOG_EDITOR") || r.equals("ROLE_BLOG_MANAGER")
                || r.equals("ROLE_ADMIN") || r.equals("ROLE_SUPERADMIN"));
        boolean canEdit = canCreate;
        boolean canDelete = roles.stream().anyMatch(r -> r.equals("ROLE_BLOG_MANAGER")
                || r.equals("ROLE_ADMIN") || r.equals("ROLE_SUPERADMIN"));

        return ResponseEntity.ok(Map.of(
                "user", userDetails.getUsername(),
                "roles", roles,
                "blogPermissions", Map.of(
                        "canCreate", canCreate,
                        "canEdit", canEdit,
                        "canDelete", canDelete)));
    }
}
