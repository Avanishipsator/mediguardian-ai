package com.mediguardian.record.service;

import com.mediguardian.record.dto.MedicalDataPointDto;
import com.mediguardian.record.entity.MetricType;
import com.mediguardian.record.repository.MedicalDataPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalDataPointService {

    private final MedicalDataPointRepository repository;

    @Transactional(readOnly = true)
    public List<MedicalDataPointDto> getFilteredDataPoints(UUID profileId, MetricType metricType, LocalDate startDate, LocalDate endDate) {
        return repository.findFilteredDataPoints(profileId, metricType, startDate, endDate).stream()
                .map(entity -> MedicalDataPointDto.builder()
                        .id(entity.getId())
                        .metricType(entity.getMetricType())
                        .metricName(entity.getMetricName())
                        .metricValue(entity.getMetricValue())
                        .unit(entity.getUnit())
                        .normalRange(entity.getNormalRange())
                        .trendStatus(entity.getTrendStatus())
                        .measurementDate(entity.getMeasurementDate())
                        .build())
                .collect(Collectors.toList());
    }
}
