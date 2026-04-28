package com.project.wms.inventory.dto;

public record InventorySummaryResponse(
    Long productId,
    String productName,
    Long warehouseId,
    String warehouseName,
    Long zoneId,
    String zoneName,
    int quantity,
    int availableQuantity,
    String status
) {}
