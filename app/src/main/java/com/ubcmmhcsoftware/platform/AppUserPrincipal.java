package com.ubcmmhcsoftware.platform;

import java.util.UUID;

/**
 * Process-local authenticated user. Replaces gateway X-User-* headers.
 */
public interface AppUserPrincipal {
    UUID getUserId();

    String getEmail();
}
