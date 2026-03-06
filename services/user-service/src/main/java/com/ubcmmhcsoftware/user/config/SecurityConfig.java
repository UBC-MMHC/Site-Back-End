package com.ubcmmhcsoftware.user.config;

import com.ubcmmhcsoftware.user.filter.GatewayClaimAuthenticationFilter;
import com.ubcmmhcsoftware.user.filter.InternalServiceAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayClaimAuthenticationFilter gatewayClaimAuthenticationFilter;
    private final InternalServiceAuthFilter internalServiceAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/user/internal/**").permitAll()
                        .requestMatchers("/api/admin/users/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/blog/**").hasAnyAuthority(
                                "ROLE_BLOG_EDITOR", "ROLE_BLOG_MANAGER", "ROLE_ADMIN", "ROLE_SUPERADMIN")
                        .requestMatchers("/api/user/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        http.addFilterBefore(internalServiceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gatewayClaimAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
