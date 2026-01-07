package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppProperties {
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.jwt-cookie.secure:true}")
    private boolean jwtCookieSecure;

    @Value("${app.jwt-cookie.same-site:None}")
    private String jwtCookieSameSite;

    @Value("${app.jwt-cookie.name:JWT}")
    private String jwtCookieName;

    @Value("${app.jwt.expiration-seconds:604800}")
    private long jwtExpirationSeconds;

    public String getRedirectAfterLogin() {
        return frontendUrl + "/auth/callback";
    }

    public String getUnauthorizedUrl() {
        return frontendUrl + "/unauthorized";
    }
}
