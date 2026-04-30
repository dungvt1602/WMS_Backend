package com.project.wms.auth.entity;

import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Role entity defining authority levels (e.g., ROLE_ADMIN, ROLE_STAFF, ROLE_VIEWER).
 */
@Entity
@Table(name = "auth_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor // cần thiết cho builder hoạt động (tạo constructor có tất cả các tham số)
public class Role extends BaseEntity {

    private String name;
}
