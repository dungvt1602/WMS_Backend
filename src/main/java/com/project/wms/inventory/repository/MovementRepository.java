package com.project.wms.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.inventory.entity.StockMovement;

public interface MovementRepository extends JpaRepository<StockMovement, Long> {

}
