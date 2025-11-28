package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Custom handler for 403 Forbidden errors.
 * <p>
 * This component is triggered specifically when:
 * 1. The user IS logged in (Authenticated).
 * 2. But the user DOES NOT have the required role (e.g., a "USER" trying to access "/admin").
 * </p>
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Intercepts the security exception and redirects the user.
     *
     * @param request               The original HTTP request.
     * @param response              The HTTP response we are building.
     * @param accessDeniedException The exception thrown by Spring Security.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
