package com.project.wms.outbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OutboundRequest(

                @NotBlank(message = "Tên khách hàng không được để trống") Long customerId,

                String note,

                @NotEmpty(message = "Đơn xuất hàng phải có ít nhất một sản phẩm") List<OutboundItemRequest> items,

                @NotBlank(message = "Mã yêu cầu không được để trống") // cái này dùng đẻ chống lặp
                String requestId) {
}