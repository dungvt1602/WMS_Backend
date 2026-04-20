package com.project.wms.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.wms.warehouse.entity.WarehouseZone;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseZoneRepository extends JpaRepository<WarehouseZone, Long> {
    Optional<WarehouseZone> findByCode(String code);
    List<WarehouseZone> findByWarehouseId(Long warehouseId);
}
