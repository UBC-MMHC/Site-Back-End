package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppProperties {
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public String getRedirectAfterLogin() {
        return frontendUrl + "/auth/callback";
    }

    public String getUnauthorizedUrl() {
        return frontendUrl + "/unauthorized";
    }
}

