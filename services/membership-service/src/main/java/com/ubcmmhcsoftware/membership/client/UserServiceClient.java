package com.ubcmmhcsoftware.membership.client;

import java.util.UUID;

/**
 * Client for User Service exists check.
 * Implementations: UserServiceClientImpl (with Resilience4j) or NoOpUserServiceClient (local dev).
 */
public interface UserServiceClient {
    boolean userExists(UUID userId);
}
