package com.mediguardian.family.controller;

import com.mediguardian.core.common.ApiResponse;
import com.mediguardian.family.dto.AddFamilyMemberRequest;
import com.mediguardian.family.dto.FamilyRequest;
import com.mediguardian.family.dto.FamilyResponse;
import com.mediguardian.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
@Tag(name = "Family", description = "Endpoints for managing family groups and relationships")
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    @Operation(summary = "Create a new Family where the authenticated user is the Head")
    public ResponseEntity<ApiResponse<FamilyResponse>> createFamily(
            @Valid @RequestBody FamilyRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(familyService.createFamily(request), "Family created successfully"));
    }

    @PostMapping("/{familyId}/members")
    @Operation(summary = "Add an existing Profile to the Family with a specific relationship")
    public ResponseEntity<ApiResponse<FamilyResponse>> addFamilyMember(
            @PathVariable UUID familyId,
            @Valid @RequestBody AddFamilyMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(familyService.addFamilyMember(familyId, request), "Member added to family"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get all families the authenticated user is a part of")
    public ResponseEntity<ApiResponse<List<FamilyResponse>>> getMyFamilies() {
        return ResponseEntity.ok(ApiResponse.success(familyService.getMyFamilies(), "Families retrieved successfully"));
    }
}
