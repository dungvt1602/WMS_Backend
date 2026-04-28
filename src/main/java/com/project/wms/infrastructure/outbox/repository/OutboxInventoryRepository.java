package com.project.wms.infrastructure.outbox.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.project.wms.infrastructure.outbox.dto.OutboxStatus;
import com.project.wms.infrastructure.outbox.entity.OutboxInventory;

public interface OutboxInventoryRepository extends JpaRepository<OutboxInventory, Long> {
    /**
     * Lấy tối đa 100 event PENDING, sắp xếp theo thứ tự tạo (FIFO).
     *
     * Tại sao giới hạn 100?
     * - Tránh 1 lần chạy xử lý quá nhiều → giữ latency ổn định
     * - Nếu tồn đọng nhiều → các lần chạy tiếp theo sẽ tiếp tục drain
     */
    List<OutboxInventory> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    /**
     * Dùng để monitor — đếm số event PENDING/DEAD để alert nếu tồn đọng.
     */
    Long countByStatus(OutboxStatus status);

    /**
     * Dùng để cleanup — xóa event SENT quá 7 ngày khỏi DB.
     * Gọi từ một @Scheduled riêng, chạy mỗi ngày 1 lần.
     */
    @Query("DELETE FROM OutboxInventory e WHERE e.status = 'SENT' AND e.created_at < :cutoff")
    void deleteOldSentEvents(java.time.LocalDateTime cutoff);
}
