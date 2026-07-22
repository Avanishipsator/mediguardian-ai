package com.mediguardian.record.dto;

import com.mediguardian.record.entity.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mediguardian.record.entity.RecordVisibility;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private UUID id;
    private UUID profileId;
    private String title;
    private RecordType type;
    private String description;
    private RecordVisibility visibility;
    private Instant uploadDate;
    private String presignedUrl;
}
