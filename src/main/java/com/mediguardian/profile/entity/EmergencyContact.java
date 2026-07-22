package com.mediguardian.profile.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContact {
    private String contactName;
    private String contactPhone;
    private String contactRelationship;
}
