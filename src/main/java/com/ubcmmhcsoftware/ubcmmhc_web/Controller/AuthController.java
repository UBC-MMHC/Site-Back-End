package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.VerificationDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login-email")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) throws MessagingException, UnsupportedEncodingException {
        authService.requestLoginCode(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body("Verification token sent to email.");
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody VerificationDto verificationDto, HttpServletResponse response) throws IOException {

        CustomUserDetails user = authService.verifyLoginCode(verificationDto);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token not found or invalid");
        }

        return null;
    }


}
