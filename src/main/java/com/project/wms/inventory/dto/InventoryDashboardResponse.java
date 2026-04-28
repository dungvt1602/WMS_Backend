package com.project.wms.inventory.dto;

import java.util.List;

public record InventoryDashboardResponse(
        long totalProducts,
        long totalWarehouses,
        long totalQuantity,
        long totalAvailableQuantity,
        long lowStockItems,
        List<StockMovementResponse> recentMovements) {
}

