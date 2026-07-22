package com.mediguardian.hospital.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PatientSearchResponse {
    private UUID profileId;
    private String firstName;
    private String lastName;
    private String profilePhotoUrl;
    private LocalDate dateOfBirth;
    private com.mediguardian.profile.entity.Gender gender;
    private String mobileNumber;
    private UUID emergencyId;
}
