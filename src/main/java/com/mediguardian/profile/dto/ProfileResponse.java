package com.mediguardian.profile.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProfileResponse {
    private UUID id;
    private UUID accountId;
    private String firstName;
    private String lastName;
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

    private UUID emergencyId;
    private String qrCodeUrl;
    private String profilePhotoUrl;
}
