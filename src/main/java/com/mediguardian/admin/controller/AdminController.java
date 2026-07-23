package com.mediguardian.admin.controller;

import com.mediguardian.account.entity.Account;
import com.mediguardian.account.entity.Role;
import com.mediguardian.admin.dto.AccountUpdateDto;
import com.mediguardian.admin.service.AdminService;
import com.mediguardian.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin control panel endpoints")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/accounts")
    @Operation(summary = "List all accounts, optionally filtered by role")
    public ResponseEntity<ApiResponse<List<Account>>> getAccounts(
            @RequestParam(required = false) Role role
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAccountsByRole(role), "Accounts fetched"));
    }

    @PostMapping("/accounts/{id}/freeze")
    @Operation(summary = "Freeze a user account")
    public ResponseEntity<ApiResponse<Void>> freezeAccount(@PathVariable UUID id) {
        adminService.freezeAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Account frozen successfully"));
    }

    @PostMapping("/accounts/{id}/resume")
    @Operation(summary = "Resume a frozen user account")
    public ResponseEntity<ApiResponse<Void>> resumeAccount(@PathVariable UUID id) {
        adminService.resumeAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Account resumed successfully"));
    }

    @PatchMapping("/accounts/{id}")
    @Operation(summary = "Update sensitive account details (email, mobile) as admin")
    public ResponseEntity<ApiResponse<Account>> updateAccount(
            @PathVariable UUID id,
            @RequestBody AccountUpdateDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateAccount(id, request), "Account updated successfully"));
    }
}
