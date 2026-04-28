package com.project.wms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class PermissionWarehouseDTO {
    public record GrantWarehousePermissionRequest(
            @NotNull(message = "userId không được null")
            Long userId,

            @NotNull(message = "warehouseId không được null")
            Long warehouseId,

            @NotBlank(message = "permissionCode không được trống")
            String permissionCode
    ) {
    }

    public record RevokeWarehousePermissionRequest(
            @NotNull(message = "userId không được null")
            Long userId,

            @NotNull(message = "warehouseId không được null")
            Long warehouseId,

            @NotBlank(message = "permissionCode không được trống")
            String permissionCode
    ) {
    }

    public record WarehousePermissionResponse(
            Long id,
            Long userId,
            String username,
            Long warehouseId,
            String warehouseName,
            String permissionCode,
            String permissionDescription,
            Long grantedBy,
            java.time.LocalDateTime grantedAt
    ) {
    }
}
