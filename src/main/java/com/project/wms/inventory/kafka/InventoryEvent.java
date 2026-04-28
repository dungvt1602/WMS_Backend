package com.project.wms.inventory.kafka;

public record InventoryEvent(
                Long warehouseid,
                Long zoneId,
                Long productid,
                int quantity,
                String actionType,
                Long userId,
                String userName,
                String orderCode) {

}
