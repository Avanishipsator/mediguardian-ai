package com.mediguardian.ai.service;

import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import com.mediguardian.record.entity.MedicalRecord;
import com.mediguardian.record.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final ProfileRepository profileRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    @org.springframework.beans.factory.annotation.Value("${spring.ai.openai.api-key:null}")
    private String configuredApiKey;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiService.class);

    public String testConnection() {
        try {
            return chatClient.prompt()
                    .user("Hello, are you connected? Respond with 'Yes, I am connected.'")
                    .call()
                    .content();
        } catch (Exception e) {
            String errorMsg = "Failed to connect to AI using API Key [" + configuredApiKey + "]. Error: " + e.getMessage();
            log.error(errorMsg, e);
            throw new BusinessException(errorMsg, ErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }

    public AiService(ChatClient.Builder builder, ProfileRepository profileRepository, MedicalRecordRepository medicalRecordRepository) {
        this.chatClient = builder.build();
        this.profileRepository = profileRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public String analyzeProgress(UUID profileId, String condition) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        List<MedicalRecord> records = medicalRecordRepository.findByProfileIdOrderByUploadDateDesc(profileId);
        if (records.isEmpty()) {
            return "No medical records available to analyze.";
        }

        String recordsSummary = records.stream()
                .map(r -> String.format("Title: %s, Date: %s, Description: %s", r.getTitle(), r.getUploadDate(), r.getDescription()))
                .collect(Collectors.joining("\n"));

        String promptText = """
                You are a medical AI assistant.
                The patient has the following medical records:
                {records}
                
                Please analyze the patient's progress regarding the condition: {condition}.
                Compare recent reports with previous ones to determine if the patient is improving, deteriorating, or stable.
                Do not provide a medical diagnosis, just summarize the data provided.
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of("records", recordsSummary, "condition", condition));

        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            System.err.println("AI Service Error in analyzeProgress: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("AI Assistant is currently unavailable. Please verify API keys and configuration on the server.", ErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }

    public String chatWithEmergencyProfile(UUID emergencyId, String doctorQuestion) {
        Profile profile = profileRepository.findByEmergencyId(emergencyId)
                .orElseThrow(() -> new BusinessException("Emergency Profile not found", ErrorCodes.NOT_FOUND));

        List<MedicalRecord> records = medicalRecordRepository.findByProfileIdOrderByUploadDateDesc(profile.getId());
        String recordsSummary = records.isEmpty() ? "None" : records.stream()
                .map(r -> String.format("- %s (Date: %s): %s", r.getTitle(), r.getUploadDate(), r.getDescription()))
                .collect(Collectors.joining("\n"));

        String patientContext = String.format("""
                Patient Name: %s %s
                Age/DOB: %s
                Gender: %s
                Blood Group: %s
                Allergies: %s
                Conditions: %s
                
                Recent Medical History & Reports:
                %s
                """, profile.getFirstName(), profile.getLastName(), profile.getDateOfBirth(), 
                profile.getGender(), profile.getBloodGroup(), 
                String.join(", ", profile.getAllergies() != null ? profile.getAllergies() : java.util.Collections.emptyList()), 
                String.join(", ", profile.getConditions() != null ? profile.getConditions() : java.util.Collections.emptyList()),
                recordsSummary);

        String promptText = """
                You are a critical care emergency AI assistant assisting a doctor.
                The following is the emergency profile of an unconscious patient:
                {patientContext}
                
                The doctor asks: {doctorQuestion}
                
                Provide a concise, medical-grade answer based strictly on the patient's profile and medical history.
                Pay careful attention to the dates of the medical reports to determine the most recent or current issues (e.g. a 2026 report is a more recent problem than a 2024 report).
                If the profile doesn't contain enough information to answer, state that clearly.
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of("patientContext", patientContext, "doctorQuestion", doctorQuestion));

        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            System.err.println("AI Service Error in chatWithEmergencyProfile: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("AI Assistant is currently unavailable. Please verify API keys and configuration on the server.", ErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }

    public String generateTriageSummary(Profile profile) {
        List<MedicalRecord> records = medicalRecordRepository.findByProfileIdOrderByUploadDateDesc(profile.getId());
        String recordsSummary = records.isEmpty() ? "None" : records.stream()
                .map(r -> String.format("- %s (Date: %s): %s", r.getTitle(), r.getUploadDate(), r.getDescription()))
                .collect(Collectors.joining("\n"));

        String patientContext = String.format("""
                Patient Name: %s %s
                Age/DOB: %s
                Gender: %s
                Blood Group: %s
                Allergies: %s
                Conditions: %s
                
                Recent Medical History & Reports:
                %s
                """, profile.getFirstName(), profile.getLastName(), profile.getDateOfBirth(), 
                profile.getGender(), profile.getBloodGroup(), 
                String.join(", ", profile.getAllergies() != null ? profile.getAllergies() : java.util.Collections.emptyList()), 
                String.join(", ", profile.getConditions() != null ? profile.getConditions() : java.util.Collections.emptyList()),
                recordsSummary);

        String promptText = """
                You are a critical care emergency AI assistant assisting a doctor.
                The following is the emergency profile of an unconscious patient:
                {patientContext}
                
                Generate a concise, 3-4 sentence triage summary of the patient's critical health information that an ER doctor must know immediately. Focus heavily on allergies, chronic conditions, and recent severe medical events. Do NOT provide a full diagnosis, just a rapid situational summary.
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of("patientContext", patientContext));

        try {
            return java.util.concurrent.CompletableFuture.supplyAsync(() -> chatClient.prompt(prompt).call().content())
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("AI Service timed out after 5 seconds while generating triage summary.");
            return "AI Triage Unavailable: The AI service took too long to respond (Timeout > 5s).";
        } catch (Exception e) {
            log.error("AI Service Error in generateTriageSummary. API Key [{}]. Error: {}", configuredApiKey, e.getMessage(), e);
            return "AI Triage Unavailable: Failed to connect to AI provider. Please verify your API Key configuration (Error: " + e.getMessage() + ")";
        }
    }
}
