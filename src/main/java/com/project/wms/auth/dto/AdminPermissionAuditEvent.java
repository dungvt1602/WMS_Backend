package com.project.wms.auth.dto;

import java.util.UUID;

import com.project.wms.auth.enums.AdminPermissionAuditAction;

public record AdminPermissionAuditEvent(
        UUID eventId,
        AdminPermissionAuditAction action,
        Long adminId,
        String adminUsername,
        Long targetUserId,
        String targetUsername,
        Long warehouseId,
        String warehouseName,
        String permissionCode,
        String permissionDescription,
        String reason
) {
}
