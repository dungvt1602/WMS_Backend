package com.project.wms.auth.entity;

import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Role entity defining authority levels (e.g., ROLE_ADMIN, ROLE_USER).
 */
@Entity
@Table(name = "auth_role")
@Getter
@Setter
public class Role extends BaseEntity {

    private String name;
}
