package com.mediguardian.emergency.controller;

import com.mediguardian.emergency.dto.EmergencyProfileResponse;
import com.mediguardian.emergency.service.EmergencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emergency")
@RequiredArgsConstructor
@Tag(name = "Emergency", description = "Emergency Profile APIs")
public class EmergencyController {

    private final EmergencyService emergencyService;

    @Operation(summary = "Get Emergency Profile by ID", description = "Fetch critical medical details by scanning a QR Code or NFC Tag. Does not require authentication, but logs access if logged in.")
    @GetMapping("/{emergencyId}")
    public ResponseEntity<EmergencyProfileResponse> getEmergencyProfile(@PathVariable UUID emergencyId) {
        return ResponseEntity.ok(emergencyService.getEmergencyProfile(emergencyId));
    }

    @Operation(summary = "Search Emergency Profile by Fingerprint", description = "For Doctors/Hospitals to identify an unconscious patient by fingerprint.")
    @org.springframework.web.bind.annotation.PostMapping(value = "/biometric-search", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_HOSPITAL')")
    public ResponseEntity<com.mediguardian.core.common.ApiResponse<EmergencyProfileResponse>> searchEmergencyProfileByFingerprint(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @org.springframework.beans.factory.annotation.Autowired com.mediguardian.profile.service.BiometricService biometricService
    ) {
        UUID emergencyId = biometricService.searchEmergencyProfileByFingerprint(file);
        return ResponseEntity.ok(com.mediguardian.core.common.ApiResponse.success(emergencyService.getEmergencyProfile(emergencyId), "Match found"));
    }
}
