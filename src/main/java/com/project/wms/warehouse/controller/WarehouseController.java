package com.project.wms.warehouse.controller;

import com.project.wms.common.response.ApiResponse;
import com.project.wms.warehouse.dto.WarehouseRequest;
import com.project.wms.warehouse.dto.WarehouseResponse;
import com.project.wms.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse response = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses() {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.getAllWarehouses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.getWarehouseById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // phải có quyền admin thì mới được update
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.updateWarehouse(id, request)));
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')") // phải có quyền admin thì mới được xóa
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable String code) {
        warehouseService.deleteWarehouse(code);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Endpoint thử thách: PATCH để disable kho bằng mã code
    @PatchMapping("/{code}/disable")
    @PreAuthorize("hasRole('ADMIN')") // phải có quyền admin thì mới có thể disable
    public ResponseEntity<ApiResponse<Void>> disableWarehouse(@PathVariable String code) {
        warehouseService.disableWarehouse(code);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}