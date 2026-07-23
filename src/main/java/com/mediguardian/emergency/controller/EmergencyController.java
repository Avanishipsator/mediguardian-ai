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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import com.mediguardian.profile.service.BiometricService;
import com.mediguardian.core.common.ApiResponse;
import java.util.List;
import com.mediguardian.emergency.dto.ScanHistoryDto;

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
    @PostMapping(value = "/biometric-search", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR', 'ROLE_HOSPITAL')")
    public ResponseEntity<ApiResponse<EmergencyProfileResponse>> searchEmergencyProfileByFingerprint(
            @RequestParam("file") MultipartFile file,
            @Autowired BiometricService biometricService
    ) {
        UUID emergencyId = biometricService.searchEmergencyProfileByFingerprint(file);
        return ResponseEntity.ok(ApiResponse.success(emergencyService.getEmergencyProfile(emergencyId), "Match found"));
    }
    
    @Operation(summary = "Get Doctor Scan History", description = "Retrieve the last 20 patients scanned by the authenticated doctor.")
    @GetMapping("/scan-history")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<ScanHistoryDto>>> getScanHistory() {
        return ResponseEntity.ok(ApiResponse.success("History fetched successfully", emergencyService.getDoctorScanHistory()));
    }
}
