package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.URLConstant;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.VerificationDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthResponsiveService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthService;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthResponsiveService authResponsiveService;

    @PostMapping("/login-email")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) throws MessagingException, UnsupportedEncodingException {
        authService.requestLoginCode(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body("Verification token sent to email.");
    }

    // Verifies token, send JWT token to cookies and redirects to base_url
    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody VerificationDto verificationDto, HttpServletResponse response) throws IOException, ServletException, AuthenticationFailedException {
        CustomUserDetails user = authService.verifyLoginCode(verificationDto);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token not found or invalid");
        }

        authResponsiveService.handleSuccessfulAuthentication(response, user, null);

        return ResponseEntity.ok(Map.of("redirectUrl", URLConstant.REDIRECT_AFTER_LOGIN));
    }


}
