package com.project.wms.inventory.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.wms.inventory.entity.StockMovement;

@Repository
public interface MovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProductIdAndWarehouseId(Long productId, Long warehouseId, Pageable pageable);
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);
    Page<StockMovement> findByWarehouseId(Long warehouseId, Pageable pageable);
    List<StockMovement> findTop10ByOrderByCreatedAtDesc();
    List<StockMovement> findTop10ByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);
}
