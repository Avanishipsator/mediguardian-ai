package com.mediguardian.admin.controller;

import com.mediguardian.account.entity.Account;
import com.mediguardian.account.entity.Role;
import com.mediguardian.account.repository.AccountRepository;
import com.mediguardian.admin.entity.DoctorReport;
import com.mediguardian.admin.repository.DoctorReportRepository;
import com.mediguardian.core.common.ApiResponse;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctor-reports")
@RequiredArgsConstructor
@Tag(name = "Doctor Reports", description = "Endpoints for reporting doctors and managing doctor access")
@SecurityRequirement(name = "bearerAuth")
public class DoctorReportController {

    private final DoctorReportRepository doctorReportRepository;
    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    @Operation(summary = "Report a Doctor", description = "Patients can report a doctor. If a doctor receives >10 reports, they are blocked.")
    @PostMapping("/{doctorId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('FAMILY_HEAD')")
    public ResponseEntity<ApiResponse<String>> reportDoctor(
            @PathVariable UUID doctorId,
            @RequestParam UUID reportingProfileId,
            @RequestParam String reason) {

        Account doctor = accountRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException("Doctor not found", ErrorCodes.NOT_FOUND));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new BusinessException("Account is not a doctor", ErrorCodes.VALIDATION_ERROR);
        }

        Profile reportingProfile = profileRepository.findById(reportingProfileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        DoctorReport report = DoctorReport.builder()
                .doctor(doctor)
                .reportingProfile(reportingProfile)
                .reason(reason)
                .build();
        doctorReportRepository.save(report);

        long reportCount = doctorReportRepository.countByDoctorId(doctorId);
        
        if (reportCount > 10) {
            doctor.setFrozen(true);
            accountRepository.save(doctor);
            return ResponseEntity.ok(ApiResponse.success("Doctor reported. The doctor has been blocked pending administrative review.", null));
        }

        return ResponseEntity.ok(ApiResponse.success("Doctor reported successfully.", null));
    }
}
