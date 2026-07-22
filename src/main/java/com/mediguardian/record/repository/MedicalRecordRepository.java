package com.mediguardian.record.repository;

import com.mediguardian.record.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    List<MedicalRecord> findByProfileIdOrderByUploadDateDesc(UUID profileId);
}
