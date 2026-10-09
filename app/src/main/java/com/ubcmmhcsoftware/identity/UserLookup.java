package com.ubcmmhcsoftware.identity;

import java.util.UUID;

/**
 * In-process user existence check. Membership uses this instead of HTTP.
 * Swap the implementation for a REST client if user is extracted later.
 */
public interface UserLookup {
    boolean exists(UUID userId);
}
