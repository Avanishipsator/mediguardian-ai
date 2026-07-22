package com.mediguardian.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Identifier (email or mobile) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
