package com.ubcmmhcsoftware.ubcmmhc_web.DTO;

import lombok.Data;

@Data
public class MemberShipDto {

    private String email;

    private String firstName;
    private String studentId;
    private Boolean newsletter;
    private String instagram;
    private Boolean joinInstaChat;
}
