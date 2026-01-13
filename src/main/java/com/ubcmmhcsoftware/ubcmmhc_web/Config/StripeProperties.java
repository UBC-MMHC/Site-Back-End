package com.ubcmmhcsoftware.ubcmmhc_web.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Stripe integration.
 */
@Component
@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeProperties {

    private String secretKey;
    private String webhookSecret;
    private Prices prices = new Prices();

    @Getter
    @Setter
    public static class Prices {
        private String ubcStudent;
        private String nonUbcStudent;
        private String nonStudent;
    }
}
