package com.ubcmmhcsoftware.membership.dto;

import com.ubcmmhcsoftware.membership.enums.MembershipType;
import com.ubcmmhcsoftware.membership.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRegistrationDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Membership type is required")
    private MembershipType membershipType;

    private String studentId;

    private String instagram;

    private boolean instagramGroupchat;

    private boolean newsletterOptIn;

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.STRIPE;
}
