package com.ubcmmhcsoftware.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayAppProperties {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.jwt-cookie.name:JWT}")
    private String jwtCookieName;

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public String getJwtCookieName() {
        return jwtCookieName;
    }
}
