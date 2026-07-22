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
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final AuthService authService;
    private final AccountRepository accountRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final StorageService storageService;
    
    @Value("${server.url:http://localhost:8081}")
    private String serverUrl;

    @Transactional
    public ProfileResponse createProfile(ProfileRequest request, boolean isSelf) {
        UUID accountId = null;
        if (isSelf) {
            accountId = SecurityUtils.getCurrentAccountId()
                    .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
            
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
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .height(request.getHeight())
                .weight(request.getWeight())
                .emergencyContact(request.getEmergencyContact())
                .allergies(request.getAllergies())
                .diseases(request.getDiseases())
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
            qrCodeUrl = storageService.generatePresignedUrl(profile.getQrCodeUrl());
        }
        
        String profilePhotoUrl = null;
        if (profile.getProfilePhotoUrl() != null) {
            profilePhotoUrl = storageService.generatePresignedUrl(profile.getProfilePhotoUrl());
        }
        
        return ProfileResponse.builder()
                .id(profile.getId())
                .accountId(profile.getAccountId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .bloodGroup(profile.getBloodGroup())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .emergencyContact(profile.getEmergencyContact())
                .allergies(profile.getAllergies())
                .diseases(profile.getDiseases())
                .emergencyId(profile.getEmergencyId())
                .qrCodeUrl(qrCodeUrl)
                .profilePhotoUrl(profilePhotoUrl)
                .build();
    }

    @Transactional
    public ProfileResponse uploadProfilePhoto(UUID profileId, org.springframework.web.multipart.MultipartFile file) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String fileKey = "profiles/" + profileId.toString() + "/photo" + fileExtension;

        String uploadedKey = storageService.uploadFile(file, fileKey);
        profile.setProfilePhotoUrl(uploadedKey);
        
        profile = profileRepository.save(profile);
        return mapToResponse(profile);
    }
}
