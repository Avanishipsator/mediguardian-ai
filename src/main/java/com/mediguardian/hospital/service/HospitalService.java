package com.mediguardian.hospital.service;

import com.mediguardian.account.entity.Account;
import com.mediguardian.account.repository.AccountRepository;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.emergency.entity.ScanHistory;
import com.mediguardian.emergency.repository.ScanHistoryRepository;
import com.mediguardian.family.entity.FamilyMember;
import com.mediguardian.family.entity.Relationship;
import com.mediguardian.family.repository.FamilyMemberRepository;
import com.mediguardian.hospital.dto.NewbornRegistrationRequest;
import com.mediguardian.hospital.dto.PatientSearchResponse;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final FamilyMemberRepository familyMemberRepository;

    public PatientSearchResponse searchPatientByMobile(String mobileNumber) {
        Account account = accountRepository.findByEmailOrMobileNumber(mobileNumber)
                .orElseThrow(() -> new BusinessException("Patient not found", ErrorCodes.NOT_FOUND));

        Profile profile = profileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException("Profile not found for this patient", ErrorCodes.NOT_FOUND));

        return PatientSearchResponse.builder()
                .profileId(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .mobileNumber(account.getMobileNumber())
                .emergencyId(profile.getEmergencyId())
                .build();
    }

    public List<ScanHistory> getRecentScanHistory(int limit) {
        UUID doctorAccountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("Not authenticated", ErrorCodes.UNAUTHORIZED));

        return scanHistoryRepository.findByDoctorAccountIdOrderByScanTimeDesc(doctorAccountId, PageRequest.of(0, limit));
    }

    @Transactional
    public UUID registerNewborn(NewbornRegistrationRequest request) {
        Profile parentProfile = profileRepository.findById(request.getParentProfileId())
                .orElseThrow(() -> new BusinessException("Parent profile not found", ErrorCodes.NOT_FOUND));

        // Find parent's primary family (assuming the first one they are a part of, or where they are head)
        List<FamilyMember> parentFamilies = familyMemberRepository.findByProfileId(parentProfile.getId());
        if (parentFamilies.isEmpty()) {
            throw new BusinessException("Parent does not belong to any family. They must create a family first.", ErrorCodes.VALIDATION_ERROR);
        }
        UUID familyIdToJoin = parentFamilies.get(0).getFamilyId();

        UUID emergencyId = UUID.randomUUID();
        Profile newborn = Profile.builder()
                .accountId(null) // Newborns don't have accounts
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .height(request.getHeight())
                .weight(request.getWeight())
                .emergencyId(emergencyId)
                .build();

        newborn = profileRepository.save(newborn);

        FamilyMember member = FamilyMember.builder()
                .familyId(familyIdToJoin)
                .profileId(newborn.getId())
                .relationshipToHead(Relationship.CHILD)
                .canViewMedicalHistory(true)
                .status(com.mediguardian.family.entity.FamilyMemberStatus.PENDING)
                .build();

        familyMemberRepository.save(member);

        return newborn.getId();
    }
}
