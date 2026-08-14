package com.ubcmmhcsoftware.user.controller;

import com.ubcmmhcsoftware.platform.AppUserPrincipal;
import com.ubcmmhcsoftware.user.dto.UserInfoResponse;
import com.ubcmmhcsoftware.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserInfoResponse profile = userService.getUserProfile(principal.getUserId());

        Map<String, Object> body = Map.of(
                "sub", profile.getSub(),
                "email", profile.getEmail(),
                "name", profile.getName() != null ? profile.getName() : "",
                "newsletterSubscription", profile.isNewsletterSubscription(),
                "roles", profile.getRoles()
        );

        return ResponseEntity.ok(body);
    }
}
