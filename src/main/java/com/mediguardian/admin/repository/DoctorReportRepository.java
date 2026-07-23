package com.mediguardian.admin.repository;

import com.mediguardian.admin.entity.DoctorReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DoctorReportRepository extends JpaRepository<DoctorReport, UUID> {
    long countByDoctorId(UUID doctorId);
}
