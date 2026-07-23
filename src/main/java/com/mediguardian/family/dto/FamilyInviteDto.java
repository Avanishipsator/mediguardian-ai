package com.mediguardian.family.dto;

import com.mediguardian.family.entity.Relationship;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class FamilyInviteDto {
    private UUID familyId;
    private String familyName;
    private String headName;
    private UUID invitedProfileId;
    private String invitedProfileName;
    private Relationship relationship;
}
