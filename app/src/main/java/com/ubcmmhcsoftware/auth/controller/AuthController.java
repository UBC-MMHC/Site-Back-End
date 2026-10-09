package com.ubcmmhcsoftware.auth.controller;

import com.ubcmmhcsoftware.auth.config.AppProperties;
import com.ubcmmhcsoftware.auth.config.CustomUserDetails;
import com.ubcmmhcsoftware.auth.dto.ForgotPasswordDTO;
import com.ubcmmhcsoftware.auth.dto.LoginDTO;
import com.ubcmmhcsoftware.auth.dto.ResetPasswordDTO;
import com.ubcmmhcsoftware.auth.service.AuthResponsiveService;
import com.ubcmmhcsoftware.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthResponsiveService authResponsiveService;
    private final AppProperties appProperties;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody LoginDTO loginDTO) {
        authService.registerUser(loginDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login-user")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response)
            throws ServletException, IOException {
        CustomUserDetails user = authService.loginUser(loginDTO);

        authResponsiveService.handleSuccessfulAuthentication(response, user, null);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        authService.forgotPassword(forgotPasswordDTO.getEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        authService.resetPassword(resetPasswordDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie jwtCookie = ResponseCookie.from(appProperties.getJwtCookieName(), "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        ResponseCookie xsrfCookie = ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, xsrfCookie.toString());

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/set-token")
    public ResponseEntity<?> setToken(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String token = body.get("token");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        ResponseCookie cookie = ResponseCookie.from(appProperties.getJwtCookieName(), token)
                .path("/")
                .httpOnly(true)
                .secure(appProperties.isJwtCookieSecure())
                .maxAge(appProperties.getJwtExpirationSeconds())
                .sameSite(appProperties.getJwtCookieSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername();

        return ResponseEntity.ok(Map.of("email", email));
    }
}
