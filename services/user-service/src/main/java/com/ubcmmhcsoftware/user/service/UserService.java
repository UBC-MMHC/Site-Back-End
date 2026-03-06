package com.ubcmmhcsoftware.user.service;

import com.ubcmmhcsoftware.user.dto.UserInfoResponse;
import com.ubcmmhcsoftware.user.entity.User;
import com.ubcmmhcsoftware.user.exception.UserNotFoundException;
import com.ubcmmhcsoftware.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for user profile operations.
 * Owns: name, newsletter_subscription, user_roles.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserInfoResponse getUserProfile(UUID userId) {
        User user = userRepository.findUserByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        List<String> roles = user.getUser_roles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return UserInfoResponse.builder()
                .sub(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .newsletterSubscription(user.isNewsletterSubscription())
                .roles(roles)
                .build();
    }
}
