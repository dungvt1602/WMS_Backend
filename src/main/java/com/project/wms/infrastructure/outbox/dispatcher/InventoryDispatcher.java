package com.project.wms.infrastructure.outbox.dispatcher;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.wms.common.enums.OrderStatus;
import com.project.wms.infrastructure.outbox.dto.OutboxStatus;
import com.project.wms.infrastructure.outbox.entity.OutboxInventory;
import com.project.wms.infrastructure.outbox.repository.OutboxInventoryRepository;
import com.project.wms.infrastructure.outbox.service.OutboxInventoryService;
import com.project.wms.inventory.kafka.InventoryEvent;
import com.project.wms.inventory.kafka.InventoryProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryDispatcher {

    private final OutboxInventoryRepository outboxInventoryRepository;
    private final OutboxInventoryService outboxInventoryService;
    private final InventoryProducer inventoryProducer;
    private final ObjectMapper objectMapper;

    /**
     * Chạy mỗi 5 giây, lấy tối đa 100 event PENDING và gửi Kafka.
     *
     * fixedDelay (không phải fixedRate):
     * - fixedDelay = chờ 5s SAU KHI lần trước chạy XONG
     * - fixedRate = chạy đúng mỗi 5s kể cả lần trước chưa xong → có thể overlap
     * → Dùng fixedDelay để tránh 2 batch chạy cùng lúc
     */
    @Scheduled(fixedDelay = 5000) // 5s (5s quét lại 1 lần)
    public void dispatchInventoryEvents() {
        // 1. Tìm 100 cái pending đang gửi trong outbox inventory

        List<OutboxInventory> pending = outboxInventoryRepository
                .findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pending.isEmpty()) {
            log.info("Không có event nào để gửi");
            return;
        }
        // biếm đếm số lượng thành công và thất bại để ghi log
        int successCount = 0;
        int failureCount = 0;

        for (OutboxInventory item : pending) {
            try {
                sendMessage(item);
                item.setStatus(OutboxStatus.SENT);
                successCount++;
            } catch (Exception e) {
                item.markRetried(e.getMessage());
                failureCount++;

                if (item.getStatus() == OutboxStatus.DEAD) {
                    log.error("Event {} đã retry tối đa lần, chuyển sang DEAD", item.getPayload());
                } else {
                    log.warn("Event {} đã retry {} lần", item.getPayload(), item.getRetryCount());
                }
            }
            // luu vao outbox
            outboxInventoryRepository.save(item);
        }

        log.info("Batch gửi inventory event xong: {} success, {} failed", successCount, failureCount);

    }

    @Scheduled(cron = "0 0 0 3 * *")
    public void cleanUp() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        outboxInventoryRepository.deleteOldSentEvents(cutoff);
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    // Hàm gửi kafka
    private void sendMessage(OutboxInventory item) throws Exception {

        // Lấy Inventory Event từ outbox
        String payloadJson = item.getPayload();
        InventoryEvent event = objectMapper.readValue(payloadJson, InventoryEvent.class);
        inventoryProducer.sendInventoryEvent(event);
    }

}
