package com.project.wms.inventory.dto;

import com.project.wms.inventory.enums.MovementType;

public record TransferStockEvent(
        Long productId,
        String productName,
        Long fromWarehouseId,
        String fromWarehouseName,
        Long fromZoneId,
        String fromZoneName,
        Long toWarehouseId,
        String toWarehouseName,
        Long toZoneId,
        String toZoneName,
        int quantity,
        String referenceCode,
        Long userId,
        MovementType movementType) {

}
