package com.project.wms.outbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OutboundItemRequest(
                @NotNull(message = "ID sản phẩm không được để trống") Long productId,

                @NotNull(message = "ID kho xuất không được để trống") Long warehouseId,

                @Min(value = 1, message = "Số lượng xuất ít nhất phải là 1") int quantity) {
}