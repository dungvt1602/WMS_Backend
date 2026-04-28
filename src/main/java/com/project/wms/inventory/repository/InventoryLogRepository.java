package com.project.wms.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.inventory.entity.InventoryLog;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    boolean existsByOrderCode(String orderCode);
}
