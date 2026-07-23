package com.mediguardian.profile.repository;

import com.mediguardian.profile.entity.InsuranceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceDetailsRepository extends JpaRepository<InsuranceDetails, UUID> {
    Optional<InsuranceDetails> findByProfileId(UUID profileId);
}
