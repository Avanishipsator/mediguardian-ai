package com.mediguardian.record.controller;

import com.mediguardian.core.common.ApiResponse;
import com.mediguardian.record.dto.MedicalRecordResponse;
import com.mediguardian.record.entity.RecordType;
import com.mediguardian.record.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Medical Records", description = "Endpoints for managing medical documents and prescriptions")
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    @PostMapping(value = "/profiles/{profileId}/records", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a medical record (PDF/Image) to a profile")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> uploadRecord(
            @PathVariable UUID profileId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("type") RecordType type,
            @RequestParam(value = "description", required = false) String description
    ) {
        MedicalRecordResponse response = recordService.uploadRecord(profileId, file, title, type, description);
        return ResponseEntity.ok(ApiResponse.success(response, "Record uploaded successfully"));
    }

    @GetMapping("/profiles/{profileId}/records")
    @Operation(summary = "Get all medical records for a profile, including S3 Presigned URLs")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getRecordsForProfile(
            @PathVariable UUID profileId
    ) {
        return ResponseEntity.ok(ApiResponse.success(recordService.getRecordsForProfile(profileId), "Records retrieved successfully"));
    }

    @PatchMapping("/records/{recordId}/visibility")
    @Operation(summary = "Update the visibility of a medical record")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> updateVisibility(
            @PathVariable UUID recordId,
            @RequestParam com.mediguardian.record.entity.RecordVisibility visibility
    ) {
        MedicalRecordResponse response = recordService.updateVisibility(recordId, visibility);
        return ResponseEntity.ok(ApiResponse.success(response, "Visibility updated successfully"));
    }
}
