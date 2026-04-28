package com.project.wms.infrastructure.outbox.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.wms.infrastructure.outbox.dto.OutboxStatus;
import com.project.wms.infrastructure.outbox.entity.OutboxInventory;
import com.project.wms.infrastructure.outbox.repository.OutboxInventoryRepository;
import com.project.wms.inventory.kafka.InventoryEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxInventoryService {
    private final OutboxInventoryRepository outboxInventoryRepository;
    private final ObjectMapper objectMapper;

    private static final String EVENT_TYPE = "wms-inventory-group";

    @Transactional
    public void saveInventoryOutboundEvent(InventoryEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxInventory newOutbox = OutboxInventory.builder()
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .eventType(event.actionType())
                    .topic(EVENT_TYPE)
                    .orderCode(event.orderCode())
                    .build();

            outboxInventoryRepository.save(newOutbox);
            log.info("Stored event in outbox: {}", event.orderCode());

        } catch (JsonProcessingException e) {
            // Lỗi serialize payload → throw để rollback transaction luôn
            // Không nên tiếp tục nếu không ghi được event
            throw new RuntimeException("Failed to serialize InventoryEvent for outbox", e);
        }
    }

}
