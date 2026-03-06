package com.ubcmmhcsoftware.jwt.spring;

import com.ubcmmhcsoftware.jwt.JwtValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for JWT validation. Import this in downstream services.
 * <p>
 * Usage: Add {@code @Import(JwtValidationConfig.class)} or ensure this is component-scanned.
 * Requires: {@code app.jwt.secret} and optionally {@code app.jwt-cookie.name} (default: JWT)
 * </p>
 */
@Configuration
public class JwtValidationConfig {

    @Bean
    public JwtValidator jwtValidator(@Value("${app.jwt.secret}") String secret) {
        return new JwtValidator(secret);
    }

    @Bean
    public JwtValidationFilter jwtValidationFilter(JwtValidator jwtValidator,
                                                   @Value("${app.jwt-cookie.name:JWT}") String jwtCookieName) {
        return new JwtValidationFilter(jwtValidator, jwtCookieName);
    }
}
