package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.Config.CustomUserDetails;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.LoginDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.DTO.VerificationDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.VerificationToken;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final JWTService jWTService;
    private final CustomUserDetailsService customUserDetailsService;
    private final HandlerMapping resourceHandlerMapping;


    // upon login we check if user is registered if no, register them, then we generate token and email it
    public void requestLoginCode(LoginDTO loginDTO) throws MessagingException, UnsupportedEncodingException {
        User user = userRepository.findUserByEmail(loginDTO.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(loginDTO.getEmail());
                    return userRepository.save(newUser);
                });

        String token = generateVerificationToken();

        VerificationToken verificationToken = new VerificationToken(token, user, 10);
        verificationTokenRepository.save(verificationToken);

        emailService.sendEmail(loginDTO.getEmail(), "Your Login Code", "Your code is: " + token);
    }

    public CustomUserDetails verifyLoginCode(VerificationDto verificationDto) {
        Optional<VerificationToken> token = verificationTokenRepository.findByToken(verificationDto.getToken());
        if (token.isPresent()) {
            CustomUserDetails user = customUserDetailsService.loadUserByUsername(token.get().getUser().getEmail());
            if (user.getUsername().equals(verificationDto.getEmail())) {
                System.out.println(verificationDto.getToken());
                verificationTokenRepository.deleteByToken(verificationDto.getToken());
                return user;
            }
        }

        return null;
    }

//    public void handleSuccessfulAuthentication(HttpServletResponse response, CustomUserDetails user) throws IOException {
//        String jwtToken = jWTService.generateToken(user);
//        Cookie cookie = new Cookie("token", jwtToken);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false); // TODO: Set to true in production (requires HTTPS)
//        cookie.setPath("/");
//        cookie.setMaxAge(120 * 60 * 60);
//
//        response.addCookie(cookie);
//
//        response.setStatus(HttpServletResponse.SC_FOUND);
//        response.sendRedirect(URLConstant.FRONTEND_URL);
//    }


    // Creates 6 digit code
    private String generateVerificationToken() {
        return new DecimalFormat("000000")
                .format(new Random().nextInt(999999));
    }


}
