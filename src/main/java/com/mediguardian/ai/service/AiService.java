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

        return chatClient.prompt(prompt).call().content();
    }

    public String chatWithEmergencyProfile(UUID emergencyId, String doctorQuestion) {
        Profile profile = profileRepository.findByEmergencyId(emergencyId)
                .orElseThrow(() -> new BusinessException("Emergency Profile not found", ErrorCodes.NOT_FOUND));

        String patientContext = String.format("""
                Patient Name: %s %s
                Age/DOB: %s
                Gender: %s
                Blood Group: %s
                Allergies: %s
                Conditions: %s
                """, profile.getFirstName(), profile.getLastName(), profile.getDateOfBirth(), 
                profile.getGender(), profile.getBloodGroup(), 
                String.join(", ", profile.getAllergies() != null ? profile.getAllergies() : java.util.Collections.emptyList()), 
                String.join(", ", profile.getConditions() != null ? profile.getConditions() : java.util.Collections.emptyList()));

        String promptText = """
                You are a critical care emergency AI assistant assisting a doctor.
                The following is the emergency profile of an unconscious patient:
                {patientContext}
                
                The doctor asks: {doctorQuestion}
                
                Provide a concise, medical-grade answer based strictly on the patient's profile. 
                If the profile doesn't contain enough information to answer, state that clearly.
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of("patientContext", patientContext, "doctorQuestion", doctorQuestion));

        return chatClient.prompt(prompt).call().content();
    }
}
