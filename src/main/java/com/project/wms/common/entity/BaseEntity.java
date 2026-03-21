package com.project.wms.common.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all entities in the system.
 * Provides common fields such as ID and auditing timestamps (Creation and Update dates).
 */
@MappedSuperclass // Tells Hibernate that this is not a table, but a base for other entities
@EntityListeners(AuditingEntityListener.class) // Enables JPA Auditing (Auto-fill dates)
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * Primary key with Auto Increment strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Records the exact time when the record was first inserted.
     * It cannot be updated after creation.
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Records the exact time when the record was last modified.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}