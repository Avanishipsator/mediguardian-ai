package com.mediguardian.record.dto;

import com.mediguardian.record.entity.MetricType;
import com.mediguardian.record.entity.TrendStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MedicalDataPointDto {
    private UUID id;
    private MetricType metricType;
    private String metricName;
    private String metricValue;
    private String unit;
    private String normalRange;
    private TrendStatus trendStatus;
    private LocalDate measurementDate;
}
