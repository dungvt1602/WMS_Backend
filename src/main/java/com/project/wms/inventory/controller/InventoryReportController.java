package com.project.wms.inventory.controller;

import com.project.wms.common.response.ApiResponse;
import com.project.wms.inventory.dto.InventoryDashboardResponse;
import com.project.wms.inventory.dto.InventorySummaryResponse;
import com.project.wms.inventory.dto.StockMovementResponse;
import com.project.wms.inventory.entity.TransferReport;
import com.project.wms.inventory.service.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports/inventory")
@RequiredArgsConstructor
public class InventoryReportController {

    private final InventoryReportService inventoryReportService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Page<InventorySummaryResponse>>> getInventorySummary(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            Pageable pageable) {
        Page<InventorySummaryResponse> response =
                inventoryReportService.getInventorySummary(warehouseId, productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getStockHistory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            Pageable pageable) {
        Page<StockMovementResponse> response =
                inventoryReportService.getStockHistory(productId, warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transfers")
    public ResponseEntity<ApiResponse<Page<TransferReport>>> getTransferHistory(
            @RequestParam(required = false) Long warehouseId,
            Pageable pageable) {
        Page<TransferReport> response = inventoryReportService.getTransferHistory(warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<InventoryDashboardResponse>> getDashboard(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer lowStockThreshold) {
        InventoryDashboardResponse response = inventoryReportService.getDashboard(warehouseId, lowStockThreshold);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
