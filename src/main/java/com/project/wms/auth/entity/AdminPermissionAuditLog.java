package com.project.wms.auth.entity;

import java.util.UUID;

import com.project.wms.auth.enums.AdminPermissionAuditAction;
import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "auth_admin_permission_audit_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_admin_permission_audit_event",
                columnNames = "event_id"
        ),
        indexes = {
                @Index(name = "idx_admin_perm_audit_admin", columnList = "admin_id"),
                @Index(name = "idx_admin_perm_audit_target_user", columnList = "target_user_id"),
                @Index(name = "idx_admin_perm_audit_warehouse", columnList = "warehouse_id"),
                @Index(name = "idx_admin_perm_audit_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPermissionAuditLog extends BaseEntity {

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AdminPermissionAuditAction action;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "admin_username", nullable = false)
    private String adminUsername;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "target_username", nullable = false)
    private String targetUsername;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "warehouse_name", nullable = false)
    private String warehouseName;

    @Column(name = "permission_code", nullable = false)
    private String permissionCode;

    @Column(name = "permission_description", nullable = false)
    private String permissionDescription;

    @Column(name = "reason")
    private String reason;
}
