package com.mediguardian.profile.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrimaryDoctor {
    private String doctorName;
    private String doctorPhone;
    private String doctorHospital;
}
