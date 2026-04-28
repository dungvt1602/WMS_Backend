package com.project.wms.inventory.dto;

import com.project.wms.inventory.enums.MovementType;
import java.time.LocalDateTime;

public record StockMovementResponse(
    Long id,
    Long productId,
    String productName,
    Long warehouseId,
    String warehouseName,
    MovementType movementType,
    int quantity,
    String referenceCode,
    LocalDateTime createdAt
) {}
