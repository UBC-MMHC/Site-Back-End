package com.ubcmmhcsoftware.auth.config;

import com.ubcmmhcsoftware.auth.service.CustomUserDetailsService;
import com.ubcmmhcsoftware.auth.service.JWTService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MyOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final CustomUserDetailsService customUserDetailsService;
    private final AppProperties appProperties;
    private final JWTService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        CustomUserDetails user = customUserDetailsService.loadUserByUsername(email);
        String jwtToken = jwtService.generateToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(appProperties.getRedirectAfterLogin())
                .queryParam("token", jwtToken)
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
