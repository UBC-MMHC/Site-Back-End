package com.ubcmmhcsoftware.auth.service;

import com.ubcmmhcsoftware.auth.config.AppProperties;
import com.ubcmmhcsoftware.auth.config.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthResponsiveService {
    private final JWTService jwtService;
    private final AppProperties appProperties;

    public void handleSuccessfulAuthentication(HttpServletResponse response, CustomUserDetails customUserDetails,
            String redirect) throws IOException {

        String jwtToken = jwtService.generateToken(customUserDetails);

        ResponseCookie cookie = ResponseCookie.from(appProperties.getJwtCookieName(), jwtToken)
                .path("/")
                .httpOnly(true)
                .secure(appProperties.isJwtCookieSecure())
                .maxAge(7 * 24 * 60 * 60)
                .sameSite(appProperties.getJwtCookieSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        if (redirect != null && !redirect.isEmpty()) {
            response.sendRedirect(redirect);
        }
    }
}
