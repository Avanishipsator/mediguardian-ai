package com.mediguardian.emergency.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ScanHistoryDto {
    private UUID id;
    private UUID scannedProfileId;
    private String scannedProfileName;
    private Instant scanTime;
}
