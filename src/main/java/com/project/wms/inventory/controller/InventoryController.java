package com.project.wms.inventory.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.wms.common.response.ApiResponse;
import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.dto.InventoryResponse;
import com.project.wms.inventory.dto.InventoryTransferRequest;
import com.project.wms.inventory.dto.InventoryTransferResponse;
import com.project.wms.inventory.service.InventoryService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory")
@AllArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    // tạo controller nhập kho
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<InventoryResponse>> addStock(@Valid @RequestBody InventoryRequest iRequest) {
        InventoryResponse response = inventoryService.addStock(iRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Tạo controller chuyển kho
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> transferStock(
            @Valid @RequestBody InventoryTransferRequest request) {
        InventoryTransferResponse response = inventoryService.transferStock(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
