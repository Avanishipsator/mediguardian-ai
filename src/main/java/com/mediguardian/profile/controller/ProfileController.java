package com.mediguardian.profile.controller;

import com.mediguardian.core.common.ApiResponse;
import com.mediguardian.profile.dto.ClaimProfileRequest;
import com.mediguardian.profile.dto.ProfileRequest;
import com.mediguardian.profile.dto.ProfileResponse;
import com.mediguardian.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints for managing medical profiles")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/me")
    @Operation(summary = "Create the primary profile for the currently authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponse>> createMyProfile(
            @Valid @RequestBody ProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(profileService.createProfile(request, true), "Primary profile created successfully"));
    }

    @PostMapping("/dependent")
    @Operation(summary = "Create a dependent profile (no account attached)")
    public ResponseEntity<ApiResponse<ProfileResponse>> createDependentProfile(
            @Valid @RequestBody ProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(profileService.createProfile(request, false), "Dependent profile created successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the primary profile for the currently authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(profileService.getMyProfile(), "Profile retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get any profile by ID (Note: actual data access is filtered at Family level, but simple fetch provided here)")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfileById(id), "Profile retrieved successfully"));
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "Claim a dependent profile by creating credentials for it")
    public ResponseEntity<ApiResponse<Void>> claimProfile(
            @PathVariable UUID id,
            @Valid @RequestBody ClaimProfileRequest request
    ) {
        profileService.claimProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Profile claimed successfully. Please login and change the temporary password."));
    }

    @PostMapping(value = "/me/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile photo for the current user")
    public ResponseEntity<ApiResponse<ProfileResponse>> uploadMyProfilePhoto(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        ProfileResponse response = profileService.uploadMyProfilePhoto(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile photo uploaded successfully"));
    }

    @PostMapping(value = "/{id}/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile photo")
    public ResponseEntity<ApiResponse<ProfileResponse>> uploadProfilePhoto(
            @PathVariable UUID id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        ProfileResponse response = profileService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile photo uploaded successfully"));
    }

    @PostMapping(value = "/me/fingerprint", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a fingerprint image for biometric emergency search")
    public ResponseEntity<ApiResponse<Void>> uploadMyFingerprint(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        profileService.uploadMyFingerprint(file);
        return ResponseEntity.ok(ApiResponse.success(null, "Fingerprint registered successfully"));
    }
}
