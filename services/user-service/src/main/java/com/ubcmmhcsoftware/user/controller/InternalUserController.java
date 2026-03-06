package com.ubcmmhcsoftware.user.controller;

import com.ubcmmhcsoftware.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal API for service-to-service calls.
 * Used by Membership Service to verify user existence before linking.
 */
@RestController
@RequestMapping("/api/user/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/exists/{userId}")
    public ResponseEntity<Void> userExists(@PathVariable UUID userId) {
        return userRepository.existsById(userId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
