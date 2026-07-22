package com.mediguardian.family.dto;

import com.mediguardian.family.entity.Relationship;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AddFamilyMemberRequest {
    @NotNull(message = "Emergency ID is required")
    private UUID emergencyId;

    @NotNull(message = "Relationship is required")
    private Relationship relationshipToHead;

    private boolean canViewMedicalHistory;
}
