package com.ubcmmhcsoftware.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Response DTO describing the authenticated user's blog permissions.
 * Replaces monolith BlogController response.
 */
@Value
@Builder
public class BlogPermissionsDTO {
    String user;
    List<String> roles;
    boolean canCreate;
    boolean canEdit;
    boolean canDelete;
}
