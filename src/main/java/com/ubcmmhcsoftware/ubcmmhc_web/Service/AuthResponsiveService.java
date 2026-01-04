package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service responsible for managing HTTP responses following successful
 * authentication events.
 * <p>
 * This service acts as a bridge between the authentication logic and the
 * client,
 * handling the generation of JWTs, secure cookie creation, and redirection
 * strategies.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthResponsiveService {
    private final JWTService jwtService;
    private final AppProperties appProperties;

    /**
     * Finalizes the authentication process by generating a JWT and attaching it
     * to a secure HTTP-Only cookie.
     * *
     * <p>
     * This method is designed to be used by both standard login flows and OAuth2
     * success handlers. It ensures that the JWT is stored securely on the
     * client side, preventing access via JavaScript (XSS protection).
     * </p>
     *
     * @param response          The HttpServletResponse to which the cookie will be
     *                          added.
     * @param customUserDetails The authenticated user principal containing details
     *                          for the JWT payload.
     * @param redirect          The URL to redirect the user to after the cookie is
     *                          set.
     *                          If null or empty, no redirection occurs (useful for
     *                          REST API login).
     * @throws IOException If an input or output exception occurs during
     *                     redirection.
     */
    public void handleSuccessfulAuthentication(HttpServletResponse response, CustomUserDetails customUserDetails,
            String redirect) throws IOException {

        String jwtToken = jwtService.generateToken(customUserDetails);

        ResponseCookie cookie = ResponseCookie.from("JWT", jwtToken)
                .path("/")
                .httpOnly(true)
                .secure(appProperties.isJwtCookieSecure())
                .maxAge(7 * 24 * 60 * 60)
                .sameSite(appProperties.getJwtCookieSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // response.setStatus(HttpServletResponse.SC_FOUND);
        if (redirect != null && !redirect.isEmpty()) {
            response.sendRedirect(redirect);
        }

    }
}
