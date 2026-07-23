package com.mediguardian.family.repository;

import com.mediguardian.family.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.mediguardian.family.entity.FamilyMemberStatus;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {
    List<FamilyMember> findByFamilyId(UUID familyId);
    List<FamilyMember> findByProfileId(UUID profileId);
    Optional<FamilyMember> findByFamilyIdAndProfileId(UUID familyId, UUID profileId);
    List<FamilyMember> findByProfileIdInAndStatus(List<UUID> profileIds, FamilyMemberStatus status);
}
