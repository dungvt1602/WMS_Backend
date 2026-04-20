package com.project.wms.inventory.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.project.wms.inventory.dto.InventoryRequest;
import com.project.wms.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final InventoryService inventoryService;

    /**
     * KafkaListener với cơ chế AckMode.MANUAL (Xác nhận thủ công)
     * Giúp đảm bảo chỉ khi DB lưu xong mới báo cho Kafka là "đã xong".
     */

    @KafkaListener(topics = "wms-inventory-event", groupId = "wms-inventory-group", concurrency = "3" // chay song song
                                                                                                      // 3 cai de nuot
                                                                                                      // tin nhan cuc
                                                                                                      // nhanh
    )
    public void consume(InventoryEvent event) {

        log.info("📩 [Kafka] Nhận tín hiệu tạo Order: {}", event.orderCode());

        try {

            // Xu li nhap kho
            if ("INBOUND".equals(event.actionType())) {
                InventoryRequest request = new InventoryRequest(event.warehouseid(),
                        event.productid(), event.quantity(), event.orderCode());
                inventoryService.addStock(request); // goi service den database de xu ly

                log.info("✅ Cập nhật Database thành công cho lệnh nhập kho: {}", event.orderCode());
            }

            // xu li xuat kho
            else if ("OUTBOUND".equals(event.actionType())) {
                InventoryRequest request = new InventoryRequest(event.warehouseid(),
                        event.productid(), event.quantity(), event.orderCode());
                inventoryService.removeStock(request); // goi service den database de xu ly

                log.info("✅ Cập nhật Database thành công cho lệnh xuất kho: {}", event.orderCode());
            }
        } catch (Exception e) {
            // TODO: handle exception

            // Nơi này sau này sẽ xử lý "Dead Letter Queue" (DLQ)
            log.error("💥 Cập nhật dính chưởng lỗi nặng, Kafka sẽ tự động Replay: {}", e.getMessage());
            // Thẩy lỗi lại để báo cho sếp Kafka biết là bị xịt
            throw e;

        }

    }

}
