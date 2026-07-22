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
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public FamilyResponse createFamily(FamilyRequest request) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found. Please create your profile first.", ErrorCodes.VALIDATION_ERROR));

        Family family = Family.builder()
                .name(request.getName())
                .headProfileId(headProfile.getId())
                .build();
        family = familyRepository.save(family);

        FamilyMember selfMember = FamilyMember.builder()
                .familyId(family.getId())
                .profileId(headProfile.getId())
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
                .orElseThrow(() -> new BusinessException("Primary profile not found.", ErrorCodes.VALIDATION_ERROR));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new BusinessException("Family not found", ErrorCodes.NOT_FOUND));

        if (!family.getHeadProfileId().equals(headProfile.getId())) {
            throw new BusinessException("Only the Head of the Family can add members", ErrorCodes.FORBIDDEN);
        }

        if (familyMemberRepository.findByFamilyIdAndProfileId(familyId, request.getProfileId()).isPresent()) {
            throw new BusinessException("Profile is already in this family", ErrorCodes.VALIDATION_ERROR);
        }

        FamilyMember member = FamilyMember.builder()
                .familyId(familyId)
                .profileId(request.getProfileId())
                .relationshipToHead(request.getRelationshipToHead())
                .canViewMedicalHistory(request.isCanViewMedicalHistory())
                .build();
        familyMemberRepository.save(member);

        return mapToResponse(family);
    }

    public List<FamilyResponse> getMyFamilies() {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        Profile headProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Primary profile not found.", ErrorCodes.VALIDATION_ERROR));

        List<FamilyMember> memberships = familyMemberRepository.findByProfileId(headProfile.getId());
        
        return memberships.stream()
                .map(m -> familyRepository.findById(m.getFamilyId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
