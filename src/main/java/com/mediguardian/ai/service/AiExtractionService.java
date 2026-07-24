package com.mediguardian.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import com.mediguardian.record.entity.MedicalDataPoint;
import com.mediguardian.record.entity.MedicalRecord;
import com.mediguardian.record.entity.MetricType;
import com.mediguardian.record.entity.TrendStatus;
import com.mediguardian.record.repository.MedicalDataPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiExtractionService {

    private final ChatClient chatClient;
    private final MedicalDataPointRepository dataPointRepository;
    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    
    public AiExtractionService(ChatClient.Builder builder, MedicalDataPointRepository dataPointRepository, ProfileRepository profileRepository, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.dataPointRepository = dataPointRepository;
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }
    
    @org.springframework.beans.factory.annotation.Value("${spring.ai.openai.api-key:null}")
    private String configuredApiKey;
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiExtractionService.class);

    @Async
    @Transactional
    public void extractDataPointsAsync(Profile profile, MedicalRecord record, String recordTextContent) {
        if (recordTextContent == null || recordTextContent.trim().isEmpty()) {
            return;
        }

        try {
            String promptText = """
                    You are a medical AI assistant.
                    A new medical report or prescription was uploaded. 
                    I need you to extract two things:
                    1. Quantitative metrics in CSV format.
                    2. Categorical profile data (allergies, conditions, medications, surgeries, implants, medicalDevices, vaccinations, familyHistory).
                    
                    For the metrics, determine the standard metric type (CBC, LIPID_PANEL, LIVER_FUNCTION, KIDNEY_FUNCTION, BLOOD_SUGAR, THYROID, VITAMINS, VITALS, or OTHER).
                    Also identify the standard desired/normal range based on standard medical guidelines.

                    Text: {text}
                    
                    Return ONLY a valid JSON object with the following structure:
                    {
                      "csvData": "metricType,metricName,value,unit,normalRange\\nCBC,Hemoglobin,12.5,g/dL,13.8-17.2",
                      "profileUpdates": {
                         "allergies": ["Peanut"],
                         "conditions": ["Asthma"],
                         "medications": ["Albuterol"],
                         "surgeries": [],
                         "implants": [],
                         "medicalDevices": [],
                         "vaccinations": [],
                         "familyHistory": []
                      }
                    }
                    """;

            PromptTemplate template = new PromptTemplate(promptText);
            Prompt prompt = template.create(Map.of("text", recordTextContent));
            
            String jsonResponse = chatClient.prompt(prompt).call().content();
            
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
            }
            if (jsonResponse.endsWith("```")) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
            }
            
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            String csvData = root.path("csvData").asText();
            if (csvData != null && !csvData.isEmpty()) {
                parseAndSaveCsv(profile, record, csvData);
            }
            
            JsonNode profileUpdates = root.path("profileUpdates");
            if (!profileUpdates.isMissingNode()) {
                mergeProfileData(profile, profileUpdates);
            }
            
        } catch (Exception e) {
            log.error("Failed to extract data points via AI. API Key [{}]. Error: {}", configuredApiKey, e.getMessage());
            profile.setAiProfileExtractionStatus("Failed: " + e.getMessage());
            profileRepository.save(profile);
        }
    }

    private void mergeProfileData(Profile profile, JsonNode updates) {
        mergeList(profile.getAllergies(), updates.path("allergies"));
        mergeList(profile.getConditions(), updates.path("conditions"));
        mergeList(profile.getMedications(), updates.path("medications"));
        mergeList(profile.getSurgeries(), updates.path("surgeries"));
        mergeList(profile.getImplants(), updates.path("implants"));
        mergeList(profile.getMedicalDevices(), updates.path("medicalDevices"));
        mergeList(profile.getVaccinations(), updates.path("vaccinations"));
        mergeList(profile.getFamilyHistory(), updates.path("familyHistory"));
        
        profileRepository.save(profile);
    }
    
    private void mergeList(List<String> existing, JsonNode newItems) {
        if (existing == null || newItems.isMissingNode() || !newItems.isArray()) return;
        
        for (JsonNode item : newItems) {
            String val = item.asText().trim();
            if (!val.isEmpty()) {
                boolean exists = existing.stream().anyMatch(e -> e.equalsIgnoreCase(val));
                if (!exists) {
                    existing.add(val);
                }
            }
        }
    }

    private void parseAndSaveCsv(Profile profile, MedicalRecord record, String csv) {
        String[] lines = csv.split("\\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("`")) continue;
            
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                try {
                    MetricType type = MetricType.valueOf(parts[0].trim().toUpperCase());
                    String name = parts[1].trim();
                    String value = parts[2].trim();
                    String unit = parts[3].trim();
                    String normalRange = parts[4].trim();
                    
                    TrendStatus trend = calculateTrend(profile.getId(), name, value);
                    
                    MedicalDataPoint dataPoint = MedicalDataPoint.builder()
                            .profile(profile)
                            .sourceRecord(record)
                            .metricType(type)
                            .metricName(name)
                            .metricValue(value)
                            .unit(unit)
                            .normalRange(normalRange)
                            .trendStatus(trend)
                            .measurementDate(LocalDate.now())
                            .build();
                            
                    dataPointRepository.save(dataPoint);
                } catch (Exception e) {
                    log.warn("Could not parse CSV line: {}", line);
                }
            }
        }
    }
    
    private TrendStatus calculateTrend(UUID profileId, String metricName, String currentValueStr) {
        List<MedicalDataPoint> history = dataPointRepository.findByProfileIdAndMetricNameOrderByMeasurementDateDesc(profileId, metricName);
        if (history.isEmpty()) {
            return TrendStatus.UNKNOWN;
        }
        
        try {
            double current = Double.parseDouble(currentValueStr);
            double previous = Double.parseDouble(history.get(0).getMetricValue());
            
            if (current == previous) return TrendStatus.STABLE;
            
            return (current > previous) ? TrendStatus.IMPROVING : TrendStatus.DETERIORATING; 
        } catch (NumberFormatException e) {
            return TrendStatus.UNKNOWN;
        }
    }
}
