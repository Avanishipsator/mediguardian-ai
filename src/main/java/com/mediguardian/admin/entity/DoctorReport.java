package com.mediguardian.admin.entity;

import com.mediguardian.account.entity.Account;
import com.mediguardian.core.common.BaseEntity;
import com.mediguardian.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Account doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_profile_id", nullable = false)
    private Profile reportingProfile;

    @Column(name = "reason")
    private String reason;
}
