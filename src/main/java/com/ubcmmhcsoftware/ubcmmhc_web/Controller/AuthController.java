package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.ForgotPasswordDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.ResetPasswordDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthResponsiveService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthService;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.NewsletterRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthResponsiveService authResponsiveService;
    private final AppProperties appProperties;
    private final NewsletterRepository newsletterRepository;

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
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO foorgotPasswordDTO) {
        authService.forgotPassword(foorgotPasswordDTO.getEmail());
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

    /**
     * Sets the JWT token as an HTTP-only cookie.
     * Used by OAuth2 flow where the token is passed via URL redirect.
     */
    @PostMapping("/set-token")
    public ResponseEntity<?> setToken(@RequestBody java.util.Map<String, String> body, HttpServletResponse response) {
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

    // If in future want password less login can use this
    // @PostMapping("/login-email")
    // public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) throws
    // MessagingException, UnsupportedEncodingException {
    // authService.requestLoginCode(loginDto);
    // return ResponseEntity.status(HttpStatus.OK).body("Verification token sent to
    // email.");
    // }

    // Verifies token, send JWT token to cookies and redirects to base_url
    // In future if want 2fa or email verification
    // @GetMapping("/verify-token")
    // public void verifyToken(@RequestParam("email") String email,
    // @RequestParam("token") String token, HttpServletResponse response) throws
    // IOException, ServletException, AuthenticationFailedException {
    // CustomUserDetails user = authService.verifyLoginCode(email, token);
    //
    // if (user == null) {
    // ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token not found or
    // invalid");
    // }
    //
    // authResponsiveService.handleSuccessfulAuthentication(response, user,
    // URLConstant.REDIRECT_AFTER_LOGIN);
    // }

    /**
     * Returns the current authenticated user's basic info.
     * Used by frontend to pre-fill forms.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername();

        boolean isSubscribed = newsletterRepository.existsByEmail(email);

        return ResponseEntity.ok(java.util.Map.of(
                "email", email,
                "newsletterSubscription", isSubscribed));
    }

}
