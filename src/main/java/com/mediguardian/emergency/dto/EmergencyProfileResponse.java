package com.mediguardian.emergency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyProfileResponse {
    private UUID profileId;
    private String firstName;
    private String lastName;
    private String profilePhotoUrl;
    private LocalDate dateOfBirth;
    private com.mediguardian.profile.entity.Gender gender;
    private com.mediguardian.profile.entity.BloodGroup bloodGroup;
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

    private UUID emergencyId;
}
