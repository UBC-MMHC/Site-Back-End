package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Main Security Configuration.
 * <p>
 * This class orchestrates the application's defense layers.
 * It defines two distinct security chains:
 * 1. API Chain: Stateless, JWT-based, strict access control.
 * 2. Web Chain: Handles OAuth2 (Google) redirects and legacy browser support.
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;
        private final JWTAuthenticationFilter jwtAuthenticationFilter;
        private final MyOAuth2SuccessHandler myOAuth2SuccessHandler;
        private final AppProperties appProperties;

        /**
         * CHAIN 1: The API Guard (@Order 1)
         * <p>
         * Handles all traffic to "/api/**" except webhook.
         * Enforces Statelessness (No Cookies/Sessions) and JWT validation.
         * </p>
         */
        @Bean
        @Order(1)
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/api/**")
                                .cors(cors -> cors.configurationSource(cors()))

                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(csrfTokenRepository())
                                                .csrfTokenRequestHandler(new ReactCsrfTokenRequestHandler())
                                                .ignoringRequestMatchers("/api/auth/**", "/api/membership/**",
                                                                "/api/stripe/**",
                                                                "/api/newsletter/add-email"))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**", "/api/csrf-token").permitAll()
                                                .requestMatchers("/api/newsletter/add-email").permitAll()
                                                .requestMatchers("/api/membership/register", "/api/membership/check")
                                                .permitAll()
                                                .requestMatchers("/api/stripe/**").permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/error").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(AbstractHttpConfigurer::disable)
                                .oauth2Login(AbstractHttpConfigurer::disable)

                                .exceptionHandling(e -> e
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * CHAIN 2: The OAuth Handler (@Order 2)
         * <p>
         * Handles traffic that falls through the API chain (mostly Google OAuth
         * redirects).
         * </p>
         */
        @Bean
        @Order(2)
        public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/**")
                                .cors(cors -> cors.configurationSource(cors()))
                                .csrf(AbstractHttpConfigurer::disable)

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                                                .requestMatchers("/api/stripe/**").permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())

                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(myOAuth2SuccessHandler))

                                .exceptionHandling(e -> e
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Global CORS Configuration.
         * <p>
         * explicit whitelist of who can talk to this API (The Frontend).
         * </p>
         */
        @Bean
        CorsConfigurationSource cors() {
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                CorsConfiguration webhookConfig = new CorsConfiguration();
                webhookConfig.setAllowedOrigins(List.of("*"));
                webhookConfig.setAllowedMethods(List.of("POST", "OPTIONS"));
                webhookConfig.setAllowedHeaders(List.of("Content-Type", "Stripe-Signature"));
                source.registerCorsConfiguration("/api/stripe/**", webhookConfig);

                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(appProperties.getFrontendUrl()));
                config.setAllowCredentials(true);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
                config.setExposedHeaders(List.of("Authorization"));
                source.registerCorsConfiguration("/**", config);

                return source;
        }

        private CsrfTokenRepository csrfTokenRepository() {
                CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();

                repository.setCookieCustomizer(cookieBuilder -> {
                        cookieBuilder
                                        .path("/")
                                        .secure(true)
                                        .sameSite("None")
                                        .httpOnly(false);
                });

                return repository;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

}
