package com.mediguardian.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceDetailsDto {
    private UUID id;
    private UUID profileId;
    private String providerName;
    private String policyNumber;
    private String groupId;
    private com.mediguardian.profile.entity.InsuranceType coverageType;
    private LocalDate expirationDate;
}
