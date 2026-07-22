package com.mediguardian.core.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
// @SQLRestriction("deleted = false") - applies to entities, not MappedSuperclass directly, so it needs to be on child entities or use a generic approach. Wait, Hibernate 6 supports it on MappedSuperclass.
@SQLRestriction("deleted = false")
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME) // UUIDv7 for DB performance
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Version
    private Long version; // Optimistic locking

    @Column(nullable = false)
    private boolean deleted = false; // Soft delete

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false, columnDefinition = "uuid")
    private UUID createdBy; // ID of the account that created this

    @LastModifiedBy
    @Column(columnDefinition = "uuid")
    private UUID updatedBy;
}
