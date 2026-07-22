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
    private String emergencyContact;
    private List<String> allergies;
    private List<String> diseases;
    private UUID emergencyId;
    private String qrCodeUrl;
    private String profilePhotoUrl;
}
