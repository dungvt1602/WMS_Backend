package com.project.wms.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(
        @NotBlank(message = "Mã kho không được để trống") String code,
        @NotBlank(message = "Tên kho không được để trống") String name,
        @NotBlank(message = "Địa chỉ kho không được để trống") String location,
        @Min(value = 0, message = "Dung lượng kho phải lớn hơn hoặc bằng 0") int capacity,
        boolean active) {

}
