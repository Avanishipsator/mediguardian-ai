package com.mediguardian.profile.entity;

import com.mediguardian.core.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile extends BaseEntity {

    @Column(name = "account_id")
    private UUID accountId; // Nullable for dependents

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    private String gender;
    
    private String bloodGroup;
    
    private Double height; // cm
    
    private Double weight; // kg

    private String emergencyContact;

    @ElementCollection
    @CollectionTable(name = "profile_allergies", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "allergy")
    @Builder.Default
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_diseases", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "disease")
    @Builder.Default
    private List<String> diseases = new ArrayList<>();

    @Column(unique = true)
    private UUID emergencyId;

    private String qrCodeUrl;

    private String profilePhotoUrl;
}
