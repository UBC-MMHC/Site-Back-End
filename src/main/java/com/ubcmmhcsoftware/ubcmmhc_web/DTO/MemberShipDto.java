package com.ubcmmhcsoftware.ubcmmhc_web.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberShipDto {
    private String email;
    private String firstName;
    private String studentId;
    private String instagram;
    private Boolean newsletter;
    private Boolean joinInstaChat;

    // TODO
    private String paymentConfirmation;

    private LocalDateTime createdAt;

}
