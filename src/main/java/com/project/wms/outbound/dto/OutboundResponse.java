package com.project.wms.outbound.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OutboundResponse(
        Long id,
        String orderCode,
        String customerName,
        String status,
        LocalDateTime orderDateTime,
        String note,
        List<OutboundItemRequest> items) {
}
