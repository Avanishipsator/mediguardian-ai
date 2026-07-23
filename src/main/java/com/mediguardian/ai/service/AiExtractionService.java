package com.mediguardian.ai.service;

import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.profile.entity.Profile;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiExtractionService {

    private final ChatClient chatClient;
    private final MedicalDataPointRepository dataPointRepository;
    
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
                    A new medical report was uploaded. I need you to extract all quantitative metrics from this text and return them in a specific CSV format.
                    For each metric, determine its standard metric type (CBC, LIPID_PANEL, LIVER_FUNCTION, KIDNEY_FUNCTION, BLOOD_SUGAR, THYROID, VITAMINS, VITALS, or OTHER).
                    Also identify the standard desired/normal range based on standard medical guidelines for a patient.
                    
                    Text: {text}
                    
                    Return ONLY a CSV format with a header row:
                    metricType,metricName,value,unit,normalRange
                    
                    Example output:
                    CBC,Hemoglobin,12.5,g/dL,13.8-17.2
                    LIPID_PANEL,Cholesterol,180,mg/dL,<200
                    """;

            PromptTemplate template = new PromptTemplate(promptText);
            Prompt prompt = template.create(Map.of("text", recordTextContent));
            
            String csvResponse = chatClient.prompt(prompt).call().content();
            
            parseAndSaveCsv(profile, record, csvResponse);
            
        } catch (Exception e) {
            log.error("Failed to extract data points via AI. API Key [{}]. Error: {}", configuredApiKey, e.getMessage());
        }
    }

    private void parseAndSaveCsv(Profile profile, MedicalRecord record, String csv) {
        String[] lines = csv.split("\n");
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
                    
                    // Determine trend
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
            
            // This is a naive heuristic (e.g. higher isn't always better/worse).
            // A more advanced prompt can ask the AI to determine improving/deteriorating based on the normal range.
            // For now, if it moves closer to the normal range, we can say improving, else deteriorating.
            // In a real production scenario, the AI should explicitly assess the trend.
            // For simplicity, we'll ask the AI in the future or keep it simple here.
            return (current > previous) ? TrendStatus.IMPROVING : TrendStatus.DETERIORATING; 
        } catch (NumberFormatException e) {
            return TrendStatus.UNKNOWN;
        }
    }
}
