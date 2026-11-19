package com.ubcmmhcsoftware.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Sends users back to the frontend login page when Google sign-in fails.
 *
 * <p>Without this, Spring falls back to redirecting to the backend's own {@code /login?error}.
 * That page is never generated here because {@link SecurityConfig} sets a custom
 * authentication entry point, so users saw a Whitelabel 404 on the backend host.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MyOAuth2FailureHandler implements AuthenticationFailureHandler {
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String code = exception instanceof OAuth2AuthenticationException oauthException
                ? oauthException.getError().getErrorCode()
                : exception.getClass().getSimpleName();
        log.warn("OAuth2 login failed ({}): {}", code, exception.getMessage());

        response.sendRedirect(appProperties.getLoginFailureUrl());
    }
}
