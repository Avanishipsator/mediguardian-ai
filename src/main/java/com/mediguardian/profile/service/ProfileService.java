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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final AuthService authService;
    private final AccountRepository accountRepository;

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
                .build();

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
                .build();
    }
}
