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
    private String mobile;
    
    private List<com.mediguardian.profile.entity.EmergencyContact> emergencyContacts;
    private com.mediguardian.profile.entity.PrimaryDoctor primaryDoctor;
    private com.mediguardian.profile.entity.Lifestyle lifestyle;

    private List<String> allergies;
    private List<String> conditions;
    private List<String> medications;
    private List<String> surgeries;
    private List<String> implants;
    private List<String> medicalDevices;
    private List<String> vaccinations;
    private List<String> familyHistory;
}
