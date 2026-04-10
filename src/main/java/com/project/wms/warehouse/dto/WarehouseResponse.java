package com.project.wms.warehouse.dto;

public record WarehouseResponse(
        Long id,
        String code,
        String name,
        String location,
        int capacity,
        boolean active) {

}
