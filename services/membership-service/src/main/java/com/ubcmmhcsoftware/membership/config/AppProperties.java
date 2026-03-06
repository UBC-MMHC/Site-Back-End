package com.ubcmmhcsoftware.membership.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppProperties {
    @Value("${app.frontend-url}")
    private String frontendUrl;
}
