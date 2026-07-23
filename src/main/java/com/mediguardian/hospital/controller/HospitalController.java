package com.mediguardian.hospital.controller;

import com.mediguardian.emergency.entity.ScanHistory;
import com.mediguardian.hospital.dto.NewbornRegistrationRequest;
import com.mediguardian.hospital.dto.PatientSearchResponse;
import com.mediguardian.hospital.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hospital")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Hospital", description = "Hospital & Doctor APIs")
public class HospitalController {

    private final HospitalService hospitalService;

    @Operation(summary = "Search patient by mobile number")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'DOCTOR')")
    @GetMapping("/patients/search")
    public ResponseEntity<PatientSearchResponse> searchPatient(@RequestParam String mobileNumber) {
        return ResponseEntity.ok(hospitalService.searchPatientByMobile(mobileNumber));
    }

    @Operation(summary = "Get recent emergency scans by doctor")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'DOCTOR')")
    @GetMapping("/scanned-history")
    public ResponseEntity<List<ScanHistory>> getScanHistory(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(hospitalService.getRecentScanHistory(limit));
    }

    @Operation(summary = "Register newborn without account")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'DOCTOR')")
    @PostMapping("/newborn")
    public ResponseEntity<UUID> registerNewborn(@RequestBody NewbornRegistrationRequest request) {
        return ResponseEntity.ok(hospitalService.registerNewborn(request));
    }
}
