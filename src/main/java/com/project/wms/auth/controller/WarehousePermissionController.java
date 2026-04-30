package com.project.wms.auth.controller;

import com.project.wms.auth.dto.PermissionWarehouseDTO.*;
import com.project.wms.auth.service.AdminPermissionService;
import com.project.wms.auth.service.PermissionWarehouseService;
import com.project.wms.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/warehouse-permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")  // Toàn bộ controller chỉ ADMIN mới vào được
public class WarehousePermissionController {

    private final AdminPermissionService warehousePermissionService;

    /**
     * Cấp quyền cho user tại 1 warehouse.
     * POST /api/v1/admin/warehouse-permissions/grant
     */
    @PostMapping("/grant")
    public ResponseEntity<ApiResponse<WarehousePermissionResponse>> grant(
            @Valid @RequestBody GrantWarehousePermissionRequest request
    ) {
        WarehousePermissionResponse response = warehousePermissionService.grant(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Thu hồi quyền của user tại 1 warehouse.
     * POST /api/v1/admin/warehouse-permissions/revoke
     */
    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @Valid @RequestBody RevokeWarehousePermissionRequest request
    ) {
        warehousePermissionService.revoke(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Xem tất cả quyền của 1 user trên mọi warehouse.
     * GET /api/v1/admin/users/{userId}/warehouse-permissions
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<WarehousePermissionResponse>>> getByUser(
            @PathVariable Long userId
    ) {
        List<WarehousePermissionResponse> response = warehousePermissionService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Xem tất cả user có quyền tại 1 warehouse.
     * GET /api/v1/admin/warehouse-permissions/warehouses/{warehouseId}
     */
    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<List<WarehousePermissionResponse>>> getByWarehouse(
            @PathVariable Long warehouseId
    ) {
        List<WarehousePermissionResponse> response = warehousePermissionService.getByWarehouse(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
