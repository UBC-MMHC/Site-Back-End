package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthResponsiveService {
    private final JWTService jwtService;

    // Called when authentication is successful, for google or email verification.
    // This is where we create JWTToken and set in cookies
    public void handleSuccessfulAuthentication(
            HttpServletResponse response, CustomUserDetails customUserDetails, String redirect)
            throws IOException, ServletException {

        String jwtToken = jwtService.generateToken(customUserDetails);

        Cookie jwtCookie = new Cookie("JWT", jwtToken);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(3600);
        jwtCookie.setSecure(false); //TODO with in prod HTTPS Req

        response.addCookie(jwtCookie);

//        response.setStatus(HttpServletResponse.SC_FOUND);
        if (redirect != null && !redirect.isEmpty()) {
            response.sendRedirect(redirect);
        }

    }
}
