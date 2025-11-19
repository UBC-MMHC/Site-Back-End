package com.ubcmmhcsoftware.ubcmmhc_web.DTO;

import lombok.Data;

@Data
public class VerificationDto {
    private String email;
    private String token;
}
