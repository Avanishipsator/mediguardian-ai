package com.mediguardian.profile.entity;

import com.mediguardian.core.common.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Embedded;
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

    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;
    
    private String mobile; // added for dependents
    
    private Double height; // cm
    
    private Double weight; // kg

    @ElementCollection
    @CollectionTable(name = "profile_emergency_contacts", joinColumns = @JoinColumn(name = "profile_id"))
    @Builder.Default
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @Embedded
    private PrimaryDoctor primaryDoctor;

    @Embedded
    private Lifestyle lifestyle;

    @ElementCollection
    @CollectionTable(name = "profile_allergies", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "allergy")
    @Builder.Default
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_conditions", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "condition")
    @Builder.Default
    private List<String> conditions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_medications", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "medication")
    @Builder.Default
    private List<String> medications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_surgeries", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "surgery")
    @Builder.Default
    private List<String> surgeries = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_implants", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "implant")
    @Builder.Default
    private List<String> implants = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_medical_devices", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "medical_device")
    @Builder.Default
    private List<String> medicalDevices = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_vaccinations", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "vaccination")
    @Builder.Default
    private List<String> vaccinations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_family_history", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "family_history_item")
    @Builder.Default
    private List<String> familyHistory = new ArrayList<>();

    @Column(unique = true)
    private UUID emergencyId;

    private String qrCodeUrl;

    private String profilePhotoUrl;

    @Column(columnDefinition = "bytea")
    private byte[] fingerprintTemplate; // Serialized SourceAFIS template
}
