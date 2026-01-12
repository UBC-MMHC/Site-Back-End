package com.ubcmmhcsoftware.ubcmmhc_web.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing the Stripe checkout session URL for frontend redirect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionDTO {

    private String sessionId;
    private String sessionUrl;
}
