package com.project.wms.inbound.dto;

import java.time.LocalDateTime;
import java.util.List;

public record InboundResponse(
        Long id,
        String orderNumber,
        String supplierName,
        String status,
        LocalDateTime orderDateTime,
        String note,
        List<InboundItemRequest> items) {
}
