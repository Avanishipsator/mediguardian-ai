package com.mediguardian.family.repository;

import com.mediguardian.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FamilyRepository extends JpaRepository<Family, UUID> {
    List<Family> findByHeadProfileId(UUID headProfileId);
}
