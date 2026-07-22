package com.mediguardian.medication.repository;

import com.mediguardian.medication.entity.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, UUID> {
    
    List<MedicationSchedule> findByProfileId(UUID profileId);
    
    @Query("SELECT m FROM MedicationSchedule m WHERE m.startDate <= :today AND (m.endDate IS NULL OR m.endDate >= :today)")
    List<MedicationSchedule> findActiveSchedules(@Param("today") LocalDate today);
}
