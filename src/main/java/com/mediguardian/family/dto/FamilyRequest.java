package com.mediguardian.family.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FamilyRequest {
    @NotBlank(message = "Family name is required")
    private String name;
}
