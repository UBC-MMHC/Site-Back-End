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

@Component
@RequiredArgsConstructor
public class MyOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthResponsiveService authResponsiveService;

    // Once authenticated using Oauth2 call handlSuccessfulAuthentication(""")
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        CustomUserDetails user = customUserDetailsService.loadUserByUsername(email);

        authResponsiveService.handleSuccessfulAuthentication(response, user, URLConstant.REDIRECT_AFTER_LOGIN);
    }

}
