package com.mediguardian.record.controller;

import com.mediguardian.core.common.ApiResponse;
import com.mediguardian.record.dto.MedicalDataPointDto;
import com.mediguardian.record.entity.MetricType;
import com.mediguardian.record.service.MedicalDataPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/medical-data/{profileId}")
@RequiredArgsConstructor
@Tag(name = "Medical Data Points", description = "Endpoints for retrieving extracted longitudinal medical data")
@SecurityRequirement(name = "bearerAuth")
public class MedicalDataPointController {

    private final MedicalDataPointService medicalDataPointService;

    @Operation(summary = "Get filtered medical data points for a profile")
    @GetMapping
    @PreAuthorize("@securityService.canAccessProfile(authentication, #profileId) or hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<MedicalDataPointDto>>> getFilteredDataPoints(
            @PathVariable UUID profileId,
            @RequestParam(required = false) MetricType metricType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            
        List<MedicalDataPointDto> points = medicalDataPointService.getFilteredDataPoints(profileId, metricType, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Medical data points fetched successfully", points));
    }
}
