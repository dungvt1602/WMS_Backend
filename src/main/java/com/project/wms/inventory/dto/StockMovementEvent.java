package com.project.wms.inventory.dto;

import com.project.wms.inventory.enums.MovementType;

public record StockMovementEvent(
                Long productId,
                String productName,
                Long zoneId,
                String zoneName,
                Long warehouseId,
                String warehouseName,
                Long userId,
                String userName,
                String referenceCode,
                MovementType movementType,
                int changeQuantity) {

}
