package com.project.wms.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

//DTO để nhận request chuyển kho
public record InventoryTransferRequest(
        @NotNull Long productId,
        @NotNull Long fromWarehouseId,
        Long fromZoneId,
        @NotNull Long toWarehouseId,
        Long toZoneId,
        @Positive int quantity,
        String referenceCode) {

}
