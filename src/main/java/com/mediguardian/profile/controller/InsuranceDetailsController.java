package com.mediguardian.profile.controller;

import com.mediguardian.core.common.ApiResponse;
import com.mediguardian.profile.dto.InsuranceDetailsDto;
import com.mediguardian.profile.dto.InsuranceDetailsRequest;
import com.mediguardian.profile.service.InsuranceDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles/{profileId}/insurance")
@RequiredArgsConstructor
@Tag(name = "Insurance Details", description = "Endpoints for managing patient insurance information")
@SecurityRequirement(name = "bearerAuth")
public class InsuranceDetailsController {

    private final InsuranceDetailsService insuranceDetailsService;

    @Operation(summary = "Get insurance details for a profile")
    @GetMapping
    @PreAuthorize("@securityService.canAccessProfile(authentication, #profileId) or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<InsuranceDetailsDto>> getInsuranceDetails(@PathVariable UUID profileId) {
        InsuranceDetailsDto details = insuranceDetailsService.getInsuranceDetails(profileId);
        return ResponseEntity.ok(ApiResponse.success(details, "Insurance details fetched successfully"));
    }

    @Operation(summary = "Create or update insurance details for a profile")
    @PutMapping
    @PreAuthorize("@securityService.canModifyProfile(authentication, #profileId)")
    public ResponseEntity<ApiResponse<InsuranceDetailsDto>> saveOrUpdateInsuranceDetails(
            @PathVariable UUID profileId,
            @RequestBody InsuranceDetailsRequest request) {
        InsuranceDetailsDto details = insuranceDetailsService.saveOrUpdateInsuranceDetails(profileId, request);
        return ResponseEntity.ok(ApiResponse.success(details, "Insurance details saved successfully"));
    }
}
