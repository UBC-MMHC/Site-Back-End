package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.AppProperties;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.ForgotPasswordDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.ResetPasswordDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthResponsiveService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
     * OAuth callback relay endpoint.
     * This endpoint is called after OAuth success with the JWT cookie already set.
     * It simply redirects to the frontend callback page.
     * Because Next.js proxies /api/*, the cookie gets passed through on the
     * frontend's domain.
     */
    @GetMapping("/oauth-callback")
    public void oauthCallback(HttpServletResponse response) throws IOException {
        response.sendRedirect(appProperties.getFrontendUrl() + "/auth/callback");
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

}
