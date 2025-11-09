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

    public void handleSuccessfulAuthentication(
            HttpServletResponse httpServletResponse, CustomUserDetails customUserDetails, String redirect)
            throws IOException, ServletException {

        String jwtToken = jwtService.generateToken(customUserDetails);

        Cookie jwtCookie = new Cookie("JWT", jwtToken);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(3600);
        jwtCookie.setSecure(false); //TODO with in prod HTTPS Req

        httpServletResponse.addCookie(jwtCookie);

        httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
        httpServletResponse.sendRedirect(redirect);
    }
}
