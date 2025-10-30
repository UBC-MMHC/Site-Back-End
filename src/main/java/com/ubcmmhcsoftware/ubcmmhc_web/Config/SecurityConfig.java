package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.JWTService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JWTService jwtService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService())
                        )
                        .successHandler(successHandler)
                );


        return http.build();
    }

    // Adds user to database if not already there
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return request -> {
            OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(request);

            String email = oauthUser.getAttribute("email");
            userRepository.findByEmail(email).orElseGet(() -> {
                User user = new User();
                user.setGoogleId(oauthUser.getAttribute("sub"));
                user.setEmail(email);
                user.setName(oauthUser.getAttribute("name"));
                user.setRole("USER");
                return userRepository.save(user);
            });

            return oauthUser;
        };
    }

    // Test Login using http://localhost:8080/oauth2/authorization/google after login redirect to localhost:3000/....
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");

            String token = jwtService.generateToken(email);

            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Authorization", "Bearer " + token);
            response.sendRedirect("http://localhost:3000?token=" + token);
        };
    }

}
