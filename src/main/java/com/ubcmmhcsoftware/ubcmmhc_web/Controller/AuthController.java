package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthResponsiveService authResponsiveService;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody LoginDTO loginDTO) {
        authService.registerUser(loginDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login-user")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response) throws ServletException, IOException {
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
        Cookie jwtCookie = new Cookie("JWT", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // TODO: Change to true in Production!
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        Cookie xsrfCookie = new Cookie("XSRF-TOKEN", null);
        xsrfCookie.setSecure(true); // TODO: Change to true in Production!
        xsrfCookie.setPath("/");
        xsrfCookie.setMaxAge(0);

        response.addCookie(jwtCookie);
        response.addCookie(xsrfCookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    // If in future want password less login can use this
//    @PostMapping("/login-email")
//    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) throws MessagingException, UnsupportedEncodingException {
//        authService.requestLoginCode(loginDto);
//        return ResponseEntity.status(HttpStatus.OK).body("Verification token sent to email.");
//    }

    // Verifies token, send JWT token to cookies and redirects to base_url
    // In future if want 2fa or email verification
//    @GetMapping("/verify-token")
//    public void verifyToken(@RequestParam("email") String email, @RequestParam("token") String token, HttpServletResponse response) throws IOException, ServletException, AuthenticationFailedException {
//        CustomUserDetails user = authService.verifyLoginCode(email, token);
//
//        if (user == null) {
//            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token not found or invalid");
//        }
//
//        authResponsiveService.handleSuccessfulAuthentication(response, user, URLConstant.REDIRECT_AFTER_LOGIN);
//    }


}
