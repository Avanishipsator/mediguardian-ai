package com.mediguardian.account.controller;

import com.mediguardian.account.dto.AuthRequest;
import com.mediguardian.account.dto.AuthResponse;
import com.mediguardian.account.dto.RegisterRequest;
import com.mediguardian.account.service.AuthService;
import com.mediguardian.core.common.ApiResponse;
import com.mediguardian.core.common.GlobalConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(GlobalConstants.AUTH_API_PREFIX)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for registering and logging into the platform")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request), "Account created successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login to the platform using email or mobile number")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(
            @Valid @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.authenticate(request), "Login successful"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change the password for the currently authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody com.mediguardian.account.dto.ChangePasswordRequest request
    ) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
