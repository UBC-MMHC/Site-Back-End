package com.ubcmmhcsoftware.user.controller;

import com.ubcmmhcsoftware.user.dto.UserInfoResponse;
import com.ubcmmhcsoftware.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private static final String X_USER_ID = "X-User-Id";

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<?> userInfo(@RequestHeader(X_USER_ID) String userId) {
        UserInfoResponse profile = userService.getUserProfile(UUID.fromString(userId));

        // Return as Map to match contract (sub, email, name, roles, newsletterSubscription)
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
