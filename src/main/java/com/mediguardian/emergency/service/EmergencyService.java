package com.mediguardian.emergency.service;

import com.mediguardian.account.entity.Account;
import com.mediguardian.account.entity.Role;
import com.mediguardian.account.repository.AccountRepository;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.emergency.dto.EmergencyProfileResponse;
import com.mediguardian.emergency.entity.ScanHistory;
import com.mediguardian.emergency.repository.ScanHistoryRepository;
import com.mediguardian.notification.service.NotificationService;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final ProfileRepository profileRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;
    private final com.mediguardian.profile.repository.InsuranceDetailsRepository insuranceDetailsRepository;
    private final com.mediguardian.ai.service.AiService aiService;

    public EmergencyProfileResponse getEmergencyProfile(UUID emergencyId) {
        Profile profile = profileRepository.findByEmergencyId(emergencyId)
                .orElseThrow(() -> new BusinessException("Emergency profile not found", ErrorCodes.NOT_FOUND));

        boolean isDoctor = SecurityUtils.hasRole("DOCTOR");
        boolean isHospital = SecurityUtils.hasRole("HOSPITAL");
        boolean hasAccess = isDoctor || isHospital;

        // Log scan history and notify if a doctor/hospital is logged in
        SecurityUtils.getCurrentAccountId().ifPresent(accountId -> {
            ScanHistory history = ScanHistory.builder()
                    .doctorAccountId(accountId)
                    .scannedProfileId(profile.getId())
                    .scanTime(Instant.now())
                    .build();
            scanHistoryRepository.save(history);
            
            Account account = accountRepository.findById(accountId).orElse(null);
            String docName = account != null ? account.getEmail() : "A Doctor";
            notificationService.createNotification(profile.getAccountId(), "Dr. " + docName + " is viewing your medical history. Please report if this is unauthorized.");
        });

        if (!hasAccess) {
            // Return limited anonymous profile
            return EmergencyProfileResponse.builder()
                    .profileId(profile.getId())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .profilePhotoUrl(profile.getProfilePhotoUrl())
                    .dateOfBirth(profile.getDateOfBirth())
                    .gender(profile.getGender())
                    .bloodGroup(profile.getBloodGroup())
                    .mobile(profile.getMobile())
                    .emergencyContacts(profile.getEmergencyContacts())
                    .emergencyId(profile.getEmergencyId())
                    .allergies(new ArrayList<>())
                    .conditions(new ArrayList<>())
                    .medications(new ArrayList<>())
                    .surgeries(new ArrayList<>())
                    .implants(new ArrayList<>())
                    .medicalDevices(new ArrayList<>())
                    .vaccinations(new ArrayList<>())
                    .familyHistory(new ArrayList<>())
                    .build();
        }

        EmergencyProfileResponse response = EmergencyProfileResponse.builder()
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
                
        // Fetch insurance info
        insuranceDetailsRepository.findByProfileId(profile.getId()).ifPresent(insurance -> {
            response.setInsuranceDetails(com.mediguardian.profile.dto.InsuranceDetailsDto.builder()
                    .id(insurance.getId())
                    .profileId(profile.getId())
                    .providerName(insurance.getProviderName())
                    .policyNumber(insurance.getPolicyNumber())
                    .groupId(insurance.getGroupId())
                    .coverageType(insurance.getCoverageType())
                    .expirationDate(insurance.getExpirationDate())
                    .build());
        });
                
        // Fetch pre-computed AI Triage Summary if requested by a doctor
        if (isDoctor) {
            response.setAiTriageSummary(profile.getAiTriageSummary());
        }
        
        return response;
    }

    public java.util.List<com.mediguardian.emergency.dto.ScanHistoryDto> getDoctorScanHistory() {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("Not authenticated", ErrorCodes.UNAUTHORIZED));
                
        return scanHistoryRepository.findByDoctorAccountIdOrderByScanTimeDesc(accountId, org.springframework.data.domain.PageRequest.of(0, 20))
                .stream()
                .map(history -> {
                    Profile profile = profileRepository.findById(history.getScannedProfileId()).orElse(null);
                    String name = profile != null ? profile.getFirstName() + " " + profile.getLastName() : "Unknown";
                    return com.mediguardian.emergency.dto.ScanHistoryDto.builder()
                            .id(history.getId())
                            .scannedProfileId(history.getScannedProfileId())
                            .scannedProfileName(name)
                            .scanTime(history.getScanTime())
                            .build();
                })
                .toList();
    }
}
