package com.project.wms.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryRequest(
                @NotNull(message = "Kho không được để trống") Long warehouseId,
                @NotNull(message = "Sản phẩm không được để trống") Long productId,
                @NotNull(message = "Số lượng không được để trống") @Positive(message = "Số lượng không được âm") int quantity,
                String referenceCode) {

}
