package com.project.wms.outbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OutboundRequest(

        @NotBlank(message = "Tên khách hàng không được để trống") String customerName,

        String note,

        @NotEmpty(message = "Đơn xuất hàng phải có ít nhất một sản phẩm") List<OutboundItemRequest> items) {
}