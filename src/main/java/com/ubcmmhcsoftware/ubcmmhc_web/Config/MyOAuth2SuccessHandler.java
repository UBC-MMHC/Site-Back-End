package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthResponsiveService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles the final step of the OAuth2 Login flow.
 * <p>
 * This class is triggered ONLY after:
 * 1. The user logs in with Google.
 * 2. Spring validates the Google token.
 * 3. {@link CustomOAuth2UserService} has saved/updated the user in the database.
 * <br>
 * Its job is to generate a JWT for the now-verified user and redirect them to the frontend.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class MyOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthResponsiveService authResponsiveService;
    private final AppProperties appProperties;

    /**
     * Invoked automatically by Spring Security when OAuth2 authentication succeeds.
     *
     * @param request        The HTTP request.
     * @param response       The HTTP response (used to add cookies and redirect).
     * @param authentication The principal object containing Google's user data.
     * @throws IOException      If redirection fails.
     * @throws ServletException If request handling fails.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        CustomUserDetails user = customUserDetailsService.loadUserByUsername(email);

        authResponsiveService.handleSuccessfulAuthentication(response, user, "/api/auth/oauth-callback");
    }

}
