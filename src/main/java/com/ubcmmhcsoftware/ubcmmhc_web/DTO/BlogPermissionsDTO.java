package com.ubcmmhcsoftware.ubcmmhc_web.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO that describes the authenticated user's blog permissions.
 */
@Data
@Builder
public class BlogPermissionsDTO {
    private String user;
    private List<String> roles;
    private boolean canCreate;
    private boolean canEdit;
    private boolean canDelete;
}
