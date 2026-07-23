package com.mediguardian.profile.entity;

import com.mediguardian.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "insurance_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceDetails extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "group_id")
    private String groupId;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "coverage_type")
    private InsuranceType coverageType;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;
}
