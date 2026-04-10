package com.project.wms.warehouse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.warehouse.entity.WarehouseEntity;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {
    // tìm kiếm kho bằng mã code
    Optional<WarehouseEntity> findByCode(String code);

    // Kiểm tra sự tồn tại của mã code
    boolean existsByCode(String code);

}
