package com.mediguardian.family.service;

import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.family.dto.AddFamilyMemberRequest;
import com.mediguardian.family.dto.FamilyRequest;
import com.mediguardian.family.dto.FamilyResponse;
import com.mediguardian.family.entity.Family;
import com.mediguardian.family.entity.FamilyMember;
import com.mediguardian.family.entity.Relationship;
import com.mediguardian.family.repository.FamilyMemberRepository;
import com.mediguardian.family.repository.FamilyRepository;
import com.mediguardian.notification.service.NotificationService;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;

    @Transactional
    public FamilyResponse createFamily(FamilyRequest request) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found. Please create your profile first.", ErrorCodes.NOT_FOUND));

        return createFamilyGroupForAccount(request.getName(), headProfile.getId());
    }

    @Transactional
    public FamilyResponse createFamilyGroupForAccount(String familyName, UUID headProfileId) {

        Family family = Family.builder()
                .name(familyName)
                .headProfileId(headProfileId)
                .build();
        family = familyRepository.save(family);

        FamilyMember selfMember = FamilyMember.builder()
                .familyId(family.getId())
                .profileId(headProfileId)
                .relationshipToHead(Relationship.SELF)
                .canViewMedicalHistory(true)
                .build();
        familyMemberRepository.save(selfMember);

        return mapToResponse(family);
    }

    @Transactional
    public FamilyResponse addFamilyMember(UUID familyId, AddFamilyMemberRequest request) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found.", ErrorCodes.NOT_FOUND));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("Family not found", ErrorCodes.NOT_FOUND));

        if (!family.getHeadProfileId().equals(headProfile.getId())) {
            throw new BusinessException("Only the Head of the Family can add members", ErrorCodes.FORBIDDEN);
        }

        Profile targetProfile = profileRepository.findByEmergencyId(request.getEmergencyId())
                .orElseThrow(() -> new BusinessException("No profile found with the provided Emergency ID", ErrorCodes.NOT_FOUND));

        if (familyMemberRepository.findByFamilyIdAndProfileId(familyId, targetProfile.getId()).isPresent()) {
            throw new BusinessException("Profile is already in this family", ErrorCodes.VALIDATION_ERROR);
        }

        FamilyMember member = FamilyMember.builder()
                .familyId(familyId)
                .profileId(targetProfile.getId())
                .relationshipToHead(request.getRelationshipToHead())
                .canViewMedicalHistory(request.isCanViewMedicalHistory())
                .status(com.mediguardian.family.entity.FamilyMemberStatus.PENDING)
                .build();
        familyMemberRepository.save(member);

        // Send notification to the owner or guardian of the target profile
        UUID targetAccountId = null;
        if (targetProfile.getAccountId() != null) {
            targetAccountId = targetProfile.getAccountId();
        } else {
            // It's a dependent, find their primary family head
            Optional<FamilyMember> targetFamilyMembership = familyMemberRepository.findByProfileId(targetProfile.getId()).stream().findFirst();
            if (targetFamilyMembership.isPresent()) {
                Family targetFamily = familyRepository.findById(targetFamilyMembership.get().getFamilyId()).orElse(null);
                if (targetFamily != null) {
                    Profile targetHeadProfile = profileRepository.findById(targetFamily.getHeadProfileId()).orElse(null);
                    if (targetHeadProfile != null) {
                        targetAccountId = targetHeadProfile.getAccountId();
                    }
                }
            }
        }
        
        if (targetAccountId != null) {
            String profileName = (targetProfile.getFirstName() != null ? targetProfile.getFirstName() : "") + " " + 
                                 (targetProfile.getLastName() != null ? targetProfile.getLastName() : "");
            String headName = (headProfile.getFirstName() != null ? headProfile.getFirstName() : "") + " " + 
                              (headProfile.getLastName() != null ? headProfile.getLastName() : "");
            String message = String.format("Family Head %s has invited %s to join their family group. Please approve or reject this request.", 
                                           headName.trim(), profileName.trim());
            notificationService.createNotification(targetAccountId, message);
        }

        return mapToResponse(family);
    }

    public List<FamilyResponse> getMyFamilies() {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found.", ErrorCodes.NOT_FOUND));

        List<FamilyMember> memberships = familyMemberRepository.findByProfileId(headProfile.getId());
        
        return memberships.stream()
                .map(m -> familyRepository.findById(m.getFamilyId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFamilyMember(UUID familyId, UUID memberProfileId) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found.", ErrorCodes.NOT_FOUND));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("Family not found", ErrorCodes.NOT_FOUND));

        if (!family.getHeadProfileId().equals(headProfile.getId())) {
            throw new BusinessException("Only the Head of the Family can remove members", ErrorCodes.FORBIDDEN);
        }

        FamilyMember member = familyMemberRepository.findByFamilyIdAndProfileId(familyId, memberProfileId)
                .orElseThrow(() -> new BusinessException("Member not found in family", ErrorCodes.NOT_FOUND));

        if (member.getRelationshipToHead() == Relationship.SELF) {
            throw new BusinessException("Cannot remove the head of the family", ErrorCodes.VALIDATION_ERROR);
        }

        familyMemberRepository.delete(member);
    }

    @Transactional
    public FamilyResponse approveFamilyMember(UUID familyId, UUID memberProfileId) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found.", ErrorCodes.NOT_FOUND));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("Family not found", ErrorCodes.NOT_FOUND));

        Profile targetProfile = profileRepository.findById(memberProfileId)
                .orElseThrow(() -> new BusinessException("Member profile not found", ErrorCodes.NOT_FOUND));

        boolean hasPermission = false;
        if (targetProfile.getAccountId() != null && targetProfile.getAccountId().equals(accountId)) {
            hasPermission = true;
        } else {
            // Check if current user is head of any family that targetProfile belongs to
            boolean isGuardian = familyMemberRepository.findByProfileId(memberProfileId).stream()
                    .map(FamilyMember::getFamilyId)
                    .map(familyRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .anyMatch(f -> f.getHeadProfileId().equals(headProfile.getId()));
            if (isGuardian) {
                hasPermission = true;
            }
        }

        if (!hasPermission) {
            throw new BusinessException("Only the invited user or their guardian can approve members", ErrorCodes.FORBIDDEN);
        }

        FamilyMember member = familyMemberRepository.findByFamilyIdAndProfileId(familyId, memberProfileId)
                .orElseThrow(() -> new BusinessException("Member not found in family", ErrorCodes.NOT_FOUND));

        member.setStatus(com.mediguardian.family.entity.FamilyMemberStatus.APPROVED);
        familyMemberRepository.save(member);

        return mapToResponse(family);
    }

    @Transactional
    public void rejectFamilyMember(UUID familyId, UUID memberProfileId) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found.", ErrorCodes.NOT_FOUND));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("Family not found", ErrorCodes.NOT_FOUND));

        Profile targetProfile = profileRepository.findById(memberProfileId)
                .orElseThrow(() -> new BusinessException("Member profile not found", ErrorCodes.NOT_FOUND));

        boolean hasPermission = false;
        if (targetProfile.getAccountId() != null && targetProfile.getAccountId().equals(accountId)) {
            hasPermission = true;
        } else {
            // Check if current user is head of any family that targetProfile belongs to
            boolean isGuardian = familyMemberRepository.findByProfileId(memberProfileId).stream()
                    .map(FamilyMember::getFamilyId)
                    .map(familyRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .anyMatch(f -> f.getHeadProfileId().equals(headProfile.getId()));
            if (isGuardian) {
                hasPermission = true;
            }
        }

        if (!hasPermission) {
            throw new BusinessException("Only the invited user or their guardian can reject members", ErrorCodes.FORBIDDEN);
        }

        FamilyMember member = familyMemberRepository.findByFamilyIdAndProfileId(familyId, memberProfileId)
                .orElseThrow(() -> new BusinessException("Member not found in family", ErrorCodes.NOT_FOUND));

        familyMemberRepository.delete(member);
    }

    private FamilyResponse mapToResponse(Family family) {
        List<FamilyMember> members = familyMemberRepository.findByFamilyId(family.getId());
        
        List<FamilyResponse.FamilyMemberDto> memberDtos = members.stream().map(m -> {
            Profile p = profileRepository.findById(m.getProfileId()).orElse(null);
            return FamilyResponse.FamilyMemberDto.builder()
                    .profileId(m.getProfileId())
                    .firstName(p != null ? p.getFirstName() : "Unknown")
                    .lastName(p != null ? p.getLastName() : "Unknown")
                    .relationshipToHead(m.getRelationshipToHead())
                    .canViewMedicalHistory(m.isCanViewMedicalHistory())
                    .status(m.getStatus())
                    .isDependent(p != null && p.getAccountId() == null)
                    .build();
        }).collect(Collectors.toList());

        return FamilyResponse.builder()
                .id(family.getId())
                .name(family.getName())
                .headProfileId(family.getHeadProfileId())
                .members(memberDtos)
                .build();
    }
}
