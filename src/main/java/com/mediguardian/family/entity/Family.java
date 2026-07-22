package com.mediguardian.family.entity;

import com.mediguardian.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "families")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Family extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "head_profile_id", nullable = false)
    private UUID headProfileId;
}
