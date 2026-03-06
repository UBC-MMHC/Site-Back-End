package com.ubcmmhcsoftware.membership.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * No-op implementation for local dev when User Service is not available.
 * Skips the user-exists check (assumes user exists).
 */
@Component
@Profile("local")
@Slf4j
public class NoOpUserServiceClient implements UserServiceClient {

    @Override
    public boolean userExists(UUID userId) {
        if (userId != null) {
            log.debug("Local profile: skipping user-exists check for {}", userId);
        }
        return true;
    }
}
