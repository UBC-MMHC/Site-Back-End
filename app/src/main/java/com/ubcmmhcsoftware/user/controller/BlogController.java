package com.ubcmmhcsoftware.user.controller;

import com.ubcmmhcsoftware.platform.AppUserPrincipal;
import com.ubcmmhcsoftware.user.dto.BlogPermissionsDTO;
import com.ubcmmhcsoftware.user.enums.BlogPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog")
public class BlogController {

    @GetMapping
    public ResponseEntity<BlogPermissionsDTO> getUserPermissions(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails instanceof AppUserPrincipal principal
                ? principal.getEmail()
                : userDetails.getUsername();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(BlogPermissionsDTO.builder()
                .user(email)
                .roles(roles)
                .canCreate(BlogPermission.CREATE.isGrantedByAny(roles))
                .canEdit(BlogPermission.EDIT.isGrantedByAny(roles))
                .canDelete(BlogPermission.DELETE.isGrantedByAny(roles))
                .build());
    }
}
