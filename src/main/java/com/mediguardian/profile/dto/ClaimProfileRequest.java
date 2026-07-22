package com.mediguardian.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClaimProfileRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;
}
