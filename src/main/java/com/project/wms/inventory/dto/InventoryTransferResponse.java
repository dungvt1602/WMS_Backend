package com.project.wms.inventory.dto;

import java.time.LocalDateTime;

public record InventoryTransferResponse(
        String status,
        String message,
        LocalDateTime timestamp) {
    public static InventoryTransferResponse success(String message) {
        return new InventoryTransferResponse("SUCCESS", message, LocalDateTime.now());
    }

    public static InventoryTransferResponse error(String message) {
        return new InventoryTransferResponse("ERROR", message, LocalDateTime.now());
    }
}
