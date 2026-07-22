package com.mediguardian.profile.repository;

import com.mediguardian.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByAccountId(UUID accountId);
    Optional<Profile> findByEmergencyId(UUID emergencyId);
}
