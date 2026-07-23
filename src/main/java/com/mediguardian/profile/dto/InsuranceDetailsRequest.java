package com.mediguardian.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceDetailsRequest {
    private String providerName;
    private String policyNumber;
    private String groupId;
    private String coverageType;
    private LocalDate expirationDate;
}
