package com.ubcmmhcsoftware.ubcmmhc_web.DTO;


import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String token;
    private String newpassword;
}
