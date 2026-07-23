package com.mediguardian.record.entity;

import com.mediguardian.core.common.BaseEntity;
import com.mediguardian.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "medical_data_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDataPoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord sourceRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type")
    private MetricType metricType;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "metric_value")
    private String metricValue;

    @Column(name = "unit")
    private String unit;

    @Column(name = "normal_range")
    private String normalRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend_status")
    private TrendStatus trendStatus;

    @Column(name = "measurement_date")
    private LocalDate measurementDate;
}
