package com.project.wms.inventory.repository;

import com.project.wms.inventory.entity.TransferReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferReportRepository extends JpaRepository<TransferReport, Long> {

    Page<TransferReport> findByFromWarehouseIdOrToWarehouseId(Long fromWarehouseId, Long toWarehouseId, Pageable pageable);
}

