package com.project.wms.outbound.dto;

public record ReservedItem(
        Long productId,
        Long zoneId,
        Long warehouseId,
        int quantity,
        boolean isHot) {
    public static ReservedItem hot(Long pId, Long zId, Long wId, int qty) {
        return new ReservedItem(pId, zId, wId, qty, true);
    }

    public static ReservedItem cold(Long pId, Long zId, Long wId, int qty) {
        return new ReservedItem(pId, zId, wId, qty, false);
    }
}