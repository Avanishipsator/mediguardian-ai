package com.mediguardian.hospital.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class NewbornRegistrationRequest {
    private UUID parentProfileId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private Double weight;
    private Double height;
}
