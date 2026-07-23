package com.mediguardian.ai.controller;

import com.mediguardian.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AI Analysis", description = "AI Medical Assistant endpoints")
public class AiController {

    private final AiService aiService;

    @Operation(summary = "Analyze patient progress based on a specific condition")
    @GetMapping("/analyze/{profileId}")
    @PreAuthorize("hasAnyRole('USER', 'DOCTOR')")
    public ResponseEntity<String> analyzeProgress(@PathVariable UUID profileId, @RequestParam String condition) {
        return ResponseEntity.ok(aiService.analyzeProgress(profileId, condition));
    }

    @Operation(summary = "Doctor asks AI a question about an emergency profile")
    @GetMapping("/emergency/{emergencyId}/chat")
    public ResponseEntity<String> chatWithEmergencyProfile(@PathVariable UUID emergencyId, @RequestParam String question) {
        return ResponseEntity.ok(aiService.chatWithEmergencyProfile(emergencyId, question));
    }
}
