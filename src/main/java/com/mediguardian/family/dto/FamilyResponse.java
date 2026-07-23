package com.mediguardian.family.dto;

import com.mediguardian.family.entity.Relationship;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FamilyResponse {
    private UUID id;
    private String name;
    private UUID headProfileId;
    private List<FamilyMemberDto> members;

    @Data
    @Builder
    public static class FamilyMemberDto {
        private UUID profileId;
        private String firstName;
        private String lastName;
        private Relationship relationshipToHead;
        private boolean canViewMedicalHistory;
        private com.mediguardian.family.entity.FamilyMemberStatus status;
        private boolean isDependent;
    }
}
