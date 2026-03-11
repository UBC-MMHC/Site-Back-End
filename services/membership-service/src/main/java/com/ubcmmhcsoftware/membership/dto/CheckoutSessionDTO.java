package com.ubcmmhcsoftware.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionDTO {

    private String sessionId;
    private String sessionUrl;
}
