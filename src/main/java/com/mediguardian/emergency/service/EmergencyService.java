package com.mediguardian.emergency.service;

import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.emergency.dto.EmergencyProfileResponse;
import com.mediguardian.emergency.entity.ScanHistory;
import com.mediguardian.emergency.repository.ScanHistoryRepository;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final ProfileRepository profileRepository;
    private final ScanHistoryRepository scanHistoryRepository;

    public EmergencyProfileResponse getEmergencyProfile(UUID emergencyId) {
        Profile profile = profileRepository.findByEmergencyId(emergencyId)
                .orElseThrow(() -> new BusinessException("Emergency profile not found", ErrorCodes.NOT_FOUND));

        // Log scan history if a doctor/hospital is logged in
        SecurityUtils.getCurrentAccountId().ifPresent(accountId -> {
            ScanHistory history = ScanHistory.builder()
                    .doctorAccountId(accountId)
                    .scannedProfileId(profile.getId())
                    .scanTime(Instant.now())
                    .build();
            scanHistoryRepository.save(history);
        });

        return EmergencyProfileResponse.builder()
                .profileId(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .bloodGroup(profile.getBloodGroup())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .mobile(profile.getMobile())
                .emergencyContacts(profile.getEmergencyContacts())
                .primaryDoctor(profile.getPrimaryDoctor())
                .lifestyle(profile.getLifestyle())
                .allergies(profile.getAllergies())
                .conditions(profile.getConditions())
                .medications(profile.getMedications())
                .surgeries(profile.getSurgeries())
                .implants(profile.getImplants())
                .medicalDevices(profile.getMedicalDevices())
                .vaccinations(profile.getVaccinations())
                .familyHistory(profile.getFamilyHistory())
                .emergencyId(profile.getEmergencyId())
                .build();
    }
}
