package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.DTO.BlogPermissionsDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for blog operations.
 * Acts as the gateway entry point for blog features.
 * <p>
 * Access requires one of: ROLE_BLOG_EDITOR, ROLE_BLOG_MANAGER, ROLE_ADMIN, or
 * ROLE_SUPERADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog")
@Slf4j
public class BlogController {

    private final BlogService blogService;

    /**
     * Returns the authenticated user's blog-related permissions.
     * what blog actions the user is allowed to perform.
     */
    @GetMapping
    public ResponseEntity<BlogPermissionsDTO> getUserPermissions(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(blogService.getUserPermissions(userDetails));
    }
}
