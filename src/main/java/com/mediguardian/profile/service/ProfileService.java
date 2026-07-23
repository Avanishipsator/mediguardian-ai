package com.mediguardian.profile.service;

import com.mediguardian.account.dto.RegisterRequest;
import com.mediguardian.account.entity.Account;
import com.mediguardian.account.entity.Role;
import com.mediguardian.account.repository.AccountRepository;
import com.mediguardian.account.service.AuthService;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.profile.dto.ClaimProfileRequest;
import com.mediguardian.profile.dto.ProfileRequest;
import com.mediguardian.profile.dto.ProfileResponse;
import com.mediguardian.profile.entity.BloodGroup;
import com.mediguardian.profile.entity.Gender;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import com.mediguardian.record.service.StorageService;
import com.mediguardian.core.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final AuthService authService;
    private final AccountRepository accountRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final StorageService storageService;
    private final com.mediguardian.profile.service.BiometricService biometricService;

    public ProfileService(ProfileRepository profileRepository, 
                          @org.springframework.context.annotation.Lazy AuthService authService, 
                          AccountRepository accountRepository, 
                          QrCodeGenerator qrCodeGenerator, 
                          StorageService storageService,
                          @org.springframework.context.annotation.Lazy com.mediguardian.profile.service.BiometricService biometricService) {
        this.profileRepository = profileRepository;
        this.authService = authService;
        this.accountRepository = accountRepository;
        this.qrCodeGenerator = qrCodeGenerator;
        this.storageService = storageService;
        this.biometricService = biometricService;
    }

    @Value("${server.url:http://localhost:8081}")
    private String serverUrl;

    @Transactional
    public ProfileResponse createProfile(ProfileRequest request, boolean isSelf) {
        UUID accountId = null;
        if (isSelf) {
            accountId = SecurityUtils.getCurrentAccountId()
                    .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
        }
        return createProfileForAccount(request, accountId);
    }

    @Transactional
    public ProfileResponse createProfileForAccount(ProfileRequest request, UUID accountId) {
        if (accountId != null) {
            if (profileRepository.findByAccountId(accountId).isPresent()) {
                throw new BusinessException("Account already has a primary profile", ErrorCodes.VALIDATION_ERROR);
            }
        }

        UUID emergencyId = UUID.randomUUID();
        
        Profile profile = Profile.builder()
                .accountId(accountId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender() != null ? Gender.valueOf(request.getGender()) : null)
                .bloodGroup(request.getBloodGroup() != null ? BloodGroup.valueOf(request.getBloodGroup()) : null)
                .height(request.getHeight())
                .weight(request.getWeight())
                .mobile(request.getMobile())
                .emergencyContacts(request.getEmergencyContacts())
                .primaryDoctor(request.getPrimaryDoctor())
                .lifestyle(request.getLifestyle())
                .allergies(request.getAllergies())
                .conditions(request.getConditions())
                .medications(request.getMedications())
                .surgeries(request.getSurgeries())
                .implants(request.getImplants())
                .medicalDevices(request.getMedicalDevices())
                .vaccinations(request.getVaccinations())
                .familyHistory(request.getFamilyHistory())
                .emergencyId(emergencyId)
                .build();

        // Generate QR Code containing the emergency URL
        String emergencyUrl = serverUrl + "/api/v1/emergency/" + emergencyId;
        byte[] qrCodeBytes = qrCodeGenerator.generateQrCode(emergencyUrl, 250, 250);
        
        // Upload QR Code to S3
        String s3Key = "qrcodes/" + emergencyId + ".png";
        storageService.uploadFile(qrCodeBytes, s3Key, "image/png");
        profile.setQrCodeUrl(s3Key); // We store the key, will generate presigned URL on fetch

        profile = profileRepository.save(profile);
        return mapToResponse(profile);
    }

    public ProfileResponse getMyProfile() {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Profile not found for current account", ErrorCodes.NOT_FOUND));

        return mapToResponse(profile);
    }

    public ProfileResponse getProfileById(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));
        return mapToResponse(profile);
    }

    @Transactional
    public void claimProfile(UUID profileId, ClaimProfileRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        if (profile.getAccountId() != null) {
            throw new BusinessException("Profile is already claimed", ErrorCodes.VALIDATION_ERROR);
        }

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(request.getEmail());
        registerRequest.setMobileNumber(request.getMobileNumber());
        registerRequest.setPassword(request.getTemporaryPassword());
        registerRequest.setRole(Role.USER);

        authService.register(registerRequest);

        Account account = accountRepository.findByEmailOrMobileNumber(request.getEmail())
                .orElseThrow(() -> new BusinessException("Failed to retrieve created account", ErrorCodes.INTERNAL_SERVER_ERROR));

        profile.setAccountId(account.getId());
        profileRepository.save(profile);
    }

    private ProfileResponse mapToResponse(Profile profile) {
        String qrCodeUrl = null;
        if (profile.getQrCodeUrl() != null) {
            qrCodeUrl = storageService.getCdnUrl(profile.getQrCodeUrl());
        }
        
        String profilePhotoUrl = null;
        if (profile.getProfilePhotoUrl() != null) {
            profilePhotoUrl = storageService.getCdnUrl(profile.getProfilePhotoUrl());
        }
        
        return ProfileResponse.builder()
                .id(profile.getId())
                .accountId(profile.getAccountId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .profilePhotoUrl(profile.getProfilePhotoUrl())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender() != null ? profile.getGender().name() : null)
                .bloodGroup(profile.getBloodGroup() != null ? profile.getBloodGroup().name() : null)
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
                .qrCodeUrl(qrCodeUrl)
                .profilePhotoUrl(profilePhotoUrl)
                .build();
    }

    @Transactional
    public ProfileResponse updateProfile(UUID id, ProfileRequest request) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        if (!SecurityUtils.hasRole("ADMIN")) {
            UUID accountId = SecurityUtils.getCurrentAccountId()
                    .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
            if (profile.getAccountId() == null || !profile.getAccountId().equals(accountId)) {
                // Check if they are part of the same family and have edit permissions (simplification: only owner or admin can edit for now)
                // In a full implementation, you'd check FamilyMember permissions for EDIT here.
                throw new BusinessException("You do not have permission to edit this profile", ErrorCodes.FORBIDDEN);
            }
        }

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) profile.setGender(Gender.valueOf(request.getGender()));
        if (request.getBloodGroup() != null) profile.setBloodGroup(BloodGroup.valueOf(request.getBloodGroup()));
        if (request.getHeight() != null) profile.setHeight(request.getHeight());
        if (request.getWeight() != null) profile.setWeight(request.getWeight());
        if (request.getMobile() != null) profile.setMobile(request.getMobile());
        if (request.getEmergencyContacts() != null) profile.setEmergencyContacts(request.getEmergencyContacts());
        if (request.getPrimaryDoctor() != null) profile.setPrimaryDoctor(request.getPrimaryDoctor());
        if (request.getLifestyle() != null) profile.setLifestyle(request.getLifestyle());
        if (request.getAllergies() != null) profile.setAllergies(request.getAllergies());
        if (request.getConditions() != null) profile.setConditions(request.getConditions());
        if (request.getMedications() != null) profile.setMedications(request.getMedications());
        if (request.getSurgeries() != null) profile.setSurgeries(request.getSurgeries());
        if (request.getImplants() != null) profile.setImplants(request.getImplants());
        if (request.getMedicalDevices() != null) profile.setMedicalDevices(request.getMedicalDevices());
        if (request.getVaccinations() != null) profile.setVaccinations(request.getVaccinations());
        if (request.getFamilyHistory() != null) profile.setFamilyHistory(request.getFamilyHistory());

        profile = profileRepository.save(profile);
        return mapToResponse(profile);
    }

    @Transactional
    public ProfileResponse uploadProfilePhoto(UUID profileId, org.springframework.web.multipart.MultipartFile file) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";
        
        if (!fileExtension.equals(".pdf") && !fileExtension.equals(".jpg") && !fileExtension.equals(".jpeg") && !fileExtension.equals(".png")) {
            throw new BusinessException("Unsupported file type. Supported types are pdf, jpg, jpeg, png.", ErrorCodes.VALIDATION_ERROR);
        }
        String fileKey = "profiles/" + profileId.toString() + "/photo_" + System.currentTimeMillis() + fileExtension;

        String uploadedKey = storageService.uploadFile(file, fileKey);
        profile.setProfilePhotoUrl(uploadedKey);
        
        profile = profileRepository.save(profile);
        return mapToResponse(profile);
    }

    public ProfileResponse uploadMyProfilePhoto(org.springframework.web.multipart.MultipartFile file) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
        Profile myProfile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Profile not found for current account", ErrorCodes.NOT_FOUND));
        return uploadProfilePhoto(myProfile.getId(), file);
    }

    public void uploadMyFingerprint(org.springframework.web.multipart.MultipartFile file) {
        UUID accountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        byte[] template = biometricService.createTemplate(file);
        profile.setFingerprintTemplate(template);
        profileRepository.save(profile);
    }
}
