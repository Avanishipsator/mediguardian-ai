package com.mediguardian.medication.entity;

import com.mediguardian.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "medication_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationSchedule extends BaseEntity {



    @Column(nullable = false)
    private UUID profileId;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false)
    private String dosage; // e.g., 650mg

    @Column(nullable = false)
    private String frequency; // e.g., BD (twice a day), OD (once a day)

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;
}
