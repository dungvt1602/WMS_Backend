package com.project.wms.inventory.dto;

public record InventoryResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        Long productId,
        String productName,
        int quantity,
        int availableQuantity) {

}
