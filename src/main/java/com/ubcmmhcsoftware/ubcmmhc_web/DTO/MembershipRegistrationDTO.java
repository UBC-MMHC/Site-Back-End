package com.ubcmmhcsoftware.ubcmmhc_web.DTO;

import com.ubcmmhcsoftware.ubcmmhc_web.Enum.MembershipType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for membership registration form submission.
 */
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
}
