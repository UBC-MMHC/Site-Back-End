package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.Config.URLConstant;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.VerificationDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthResponsiveService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthService;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.angus.mail.iap.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

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

        authResponsiveService.handleSuccessfulAuthentication(response, user , null);

        return ResponseEntity.status(HttpStatus.OK).build();
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
