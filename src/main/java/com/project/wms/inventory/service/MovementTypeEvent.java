package com.project.wms.inventory.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.project.wms.auth.entity.User;
import com.project.wms.auth.repository.UserRepository;
import com.project.wms.inventory.dto.StockMovementEvent;
import com.project.wms.inventory.dto.TransferStockEvent;
import com.project.wms.inventory.entity.StockMovement;
import com.project.wms.inventory.entity.StockTransferMovement;
import com.project.wms.inventory.entity.TransferReport;
import com.project.wms.inventory.repository.MovementRepository;
import com.project.wms.inventory.repository.TransferReportRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementTypeEvent { // Class nay dung de cap nhap log

    private final MovementRepository movementRepository; // Khai bao repository de luu vao DB
    private final TransferReportRepository transferReportRepository;

    // HAM NAY dung de ghi log bien dong so du cua inbound va outbound thoi
    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // sau khi commit xong thi moi goi ham nay
    public void handleStockMovementEvent(StockMovementEvent event) {
        log.info("Ghi log bien dong so du cho: " + event.productName());
        // ta cung ket noi database
        StockMovement stockMovement = StockMovement.builder()
                .productId(event.productId())
                .productName(event.productName())
                .zoneId(event.zoneId())
                .zoneName(event.zoneName())
                .warehouseId(event.warehouseId())
                .warehouseName(event.warehouseName())
                .userId(event.userId())
                .userName(event.userName())
                .referenceCode(event.referenceCode())
                .movementType(event.movementType())
                .quantity(event.changeQuantity())
                .build();
        // luu vao bang stock_movement
        try {
            movementRepository.save(stockMovement);
            log.info("Da ghi log bien dong so du cho: " + event.productName());
        } catch (Exception e) {
            log.error("Fail to save movement", e);
        }

    }

    // ham nay xau de luu bien dong so du cua transfer zone chuyen kho noi bo va
    // chuyen kho ngoai
    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // sau khi commit xong thi moi goi ham nay
    public void handleStockTransferMovementEvent(TransferStockEvent event) {
        log.info("Ghi log bien dong so du cho: " + event.productName());
        // ta cung ket noi database
        TransferReport transferReport = TransferReport.builder()
                .productId(event.productId())
                .productName(event.productName())
                .fromZoneId(event.fromZoneId())
                .fromZoneName(event.fromZoneName())
                .fromWarehouseId(event.fromWarehouseId())
                .fromWarehouseName(event.fromWarehouseName())
                .toZoneId(event.toZoneId())
                .toZoneName(event.toZoneName())
                .toWarehouseId(event.toWarehouseId())
                .toWarehouseName(event.toWarehouseName())
                .userId(event.userId())
                .movementType(event.movementType())
                .quantity(event.quantity())
                .build();
        try {
            transferReportRepository.save(transferReport);
            log.info("Da ghi log bien dong so du cho: " + event.productName());
        } catch (Exception e) {
            log.error("Fail to save movement", e);
        }
    }
}
