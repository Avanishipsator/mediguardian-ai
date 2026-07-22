package com.mediguardian.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProfileRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;
    
    private String gender;
    private String bloodGroup;
    private Double height;
    private Double weight;
    private String emergencyContact;
    private List<String> allergies;
    private List<String> diseases;
}
