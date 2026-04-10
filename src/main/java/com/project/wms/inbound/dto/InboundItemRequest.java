package com.project.wms.inbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InboundItemRequest(
        @NotNull(message = "ID sản phẩm không được để trống") Long productId,

        @NotNull(message = "ID kho nhập không được để trống") Long warehouseId,

        @Min(value = 1, message = "Số lượng nhập ít nhất phải là 1") int quantity) {
}