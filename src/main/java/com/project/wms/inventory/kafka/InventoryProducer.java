package com.project.wms.inventory.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendInventoryEvent(InventoryEvent event) {
        kafkaTemplate.send("wms-inventory-event", event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("📤 Gửi sự kiện thành công: {}", event.orderCode());
                    } else {
                        log.error("💥 Gửi sự kiện thất bại: {}", ex.getMessage());
                    }
                });
    }
}
