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
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private Double height;
    private Double weight;
    private String emergencyContact;
    private List<String> allergies;
    private List<String> diseases;
    private UUID emergencyId;
}
