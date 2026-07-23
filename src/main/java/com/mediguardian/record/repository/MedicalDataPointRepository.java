package com.mediguardian.record.repository;

import com.mediguardian.record.entity.MedicalDataPoint;
import com.mediguardian.record.entity.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalDataPointRepository extends JpaRepository<MedicalDataPoint, UUID> {
    
    @Query("SELECT m FROM MedicalDataPoint m WHERE m.profile.id = :profileId " +
           "AND (:metricType IS NULL OR m.metricType = :metricType) " +
           "AND (:startDate IS NULL OR m.measurementDate >= :startDate) " +
           "AND (:endDate IS NULL OR m.measurementDate <= :endDate) " +
           "ORDER BY m.measurementDate DESC")
    List<MedicalDataPoint> findFilteredDataPoints(
            @Param("profileId") UUID profileId,
            @Param("metricType") MetricType metricType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
            
    List<MedicalDataPoint> findByProfileIdAndMetricNameOrderByMeasurementDateDesc(UUID profileId, String metricName);
}
