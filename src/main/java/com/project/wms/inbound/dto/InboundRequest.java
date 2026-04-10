package com.project.wms.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InboundRequest(
        @NotBlank(message = "Tên nhà cung cấp không được để trống") String supplierName,

        String note,

        @NotEmpty(message = "Đơn nhập hàng phải có ít nhất một sản phẩm") List<InboundItemRequest> items) {
}