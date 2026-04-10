package com.project.wms.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @NotBlank(message = "Tên sản phẩm không được để trống") String name,

        @NotBlank(message = "Mã sản phẩm không được để trống") String sku,

        @NotBlank(message = "Mô tả sản phẩm không được để trống") String description,

        @NotBlank(message = "Đơn vị tính không được để trống") String unit,

        @NotNull(message = "Giá sản phẩm không được để trống") @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0") BigDecimal price,

        @NotNull(message = "Số lượng không được để trống") @Min(value = 0, message = "Số lượng không được âm") int quantity) {

}
